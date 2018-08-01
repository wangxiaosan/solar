package com.wwy.eureka.redis;

import com.wwy.common.lang.Configs;
import com.wwy.common.lang.NamedThreadFactory;
import com.wwy.common.lang.NodeRegistryException;
import com.wwy.common.lang.constants.ConfigKey;
import com.wwy.common.lang.constants.Constants;
import com.wwy.common.lang.constants.LogMarker;
import com.wwy.eureka.api.AbstractFailbackRegistry;
import com.wwy.eureka.api.NodeRegistryUtils;
import com.wwy.eureka.api.NotifyListener;
import com.wwy.eureka.api.cluster.Node;
import com.wwy.eureka.api.cluster.NodeConfig;
import org.joda.time.DateTime;
import org.slf4j.MarkerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.wwy.common.lang.utils.CollectionUtils.isNotEmpty;

/**
 * @Author:weiyang.wang
 * @Date: 2018/6/27
 */
public class RedisRegistry extends AbstractFailbackRegistry {

	private final Map<String, JedisPool> jedisPoolMap = new ConcurrentHashMap<>();
	private String nodeGroup;
	private final ScheduledExecutorService expireExecutor = new ScheduledThreadPoolExecutor(1,  new NamedThreadFactory("JPTRedisRegistryExpireTimer"));
	private final ScheduledFuture<?> expireFuture;
	private final int expirePeriod;
	private boolean replicate;
	private final int reconnectPeriod;
	private final ConcurrentHashMap<String, Notifier> notifiers = new ConcurrentHashMap<>();
	private RedisLock redisLock;

	private final String FAILOVER_CLUSTER = "failover";
	private final String REPLICATE_CLUSTER = "replicate";

	public RedisRegistry(Node currentNode) {
		super(currentNode);
		NodeConfig config = currentNode.getConfig();
		this.nodeGroup = config.getNodeGroup();
		// 锁两分钟过期
		this.redisLock = new RedisLock("JPT_CLEAN_LOCK_KEY", config.getIdentity(), 2 * 60);
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		// 可以添加额外的参数

		String address = NodeRegistryUtils.getRealRegistryAddress(config.getRegistryAddress());
		String cluster = Configs.getString("cluster", "failover");
		if (!FAILOVER_CLUSTER.equals(cluster) && !REPLICATE_CLUSTER.equals(cluster)) {
			throw new IllegalArgumentException("Unsupported redis cluster: " + cluster + ". The redis cluster only supported failover or replicate.");
		}
		replicate = REPLICATE_CLUSTER.equals(cluster);
		this.reconnectPeriod = Configs.getInt(ConfigKey.REGISTRY_RECONNECT_PERIOD_KEY, Constants.DEFAULT_REGISTRY_RECONNECT_PERIOD);

		String[] adds = address.split(",");
		for (String add : adds) {
			int i = add.indexOf(":");
			String host = add;
			int port = 6379;
			if (i > 0) {
				host = add.substring(0, i);
				port = Integer.parseInt(add.substring(i + 1));
			}
			this.jedisPoolMap.put(add, new JedisPool(jedisPoolConfig, host, port, Constants.DEFAULT_TIMEOUT));
		}

		this.expirePeriod = Configs.getInt(ConfigKey.REDIS_SESSION_TIMEOUT, Constants.DEFAULT_SESSION_TIMEOUT);
		this.expireFuture = expireExecutor.scheduleWithFixedDelay(() -> {
			try {
				// 延长过期时间
				deferExpired();
			} catch (Throwable t) {
				// 防御性容错
				LOGGER.error(MarkerFactory.getMarker(LogMarker.PLATFORM), "Unexpected exception occur at defer expire time, cause: " + t.getMessage(), t);
			}
		}, expirePeriod / 2, expirePeriod / 2, TimeUnit.MICROSECONDS);
	}

	private void deferExpired() {
		for (Map.Entry<String, JedisPool> jedisPoolEntry : jedisPoolMap.entrySet()) {
			JedisPool jedisPool = jedisPoolEntry.getValue();
			try (Jedis jedis = jedisPool.getResource()) {
				for (Node node : getRegistered()) {
					String key = NodeRegistryUtils.getNodeTypePath(nodeGroup, node.getConfig().getNodeType());
					if (jedis.hset(key, node.toFullString(), String.valueOf(System.currentTimeMillis() + expirePeriod)).intValue() == 1) {
						jedis.publish(key, Constants.REGISTER);
					}
				}
				if (redisLock.acquire(jedis)) {
					clean(jedis);
				}
				if (!replicate) {
					break;
				}
			} catch (Throwable t) {
				LOGGER.warn(MarkerFactory.getMarker(LogMarker.PLATFORM), "Failed to write provider heartbeat to redis registry. registry: " + jedisPoolEntry.getKey() + ", cause: " + t.getMessage(), t);
			}
		}
	}

	private void clean(Jedis jedis) {
		// /JPT/{集群名字}/NODeS/
		Set<String> nodeTypePaths = jedis.keys(NodeRegistryUtils.getRootPath(currentNode.getConfig().getNodeGroup()) + "/*");
		if (isNotEmpty(nodeTypePaths)) {
			for (String nodeTypePath : nodeTypePaths) {
				// /JPT/{集群名字}/NODES/WORKER
				Set<String> nodePaths = jedis.keys(nodeTypePath);
				if (isNotEmpty(nodePaths)) {
					for (String nodePath : nodePaths) {
						Map<String, String> nodes = jedis.hgetAll(nodePath);
						if (isNotEmpty(nodes)) {
							boolean delete = false;
							long now = System.currentTimeMillis();
							for (Map.Entry<String, String> entry : nodes.entrySet()) {
								String key = entry.getKey();
								long expire = Long.parseLong(entry.getValue());
								if (expire < now) {
									jedis.hdel(nodePath, key);
									delete = true;
									if (LOGGER.isWarnEnabled()) {
										LOGGER.warn(MarkerFactory.getMarker(LogMarker.PLATFORM), "Delete expired key: " + nodePath + " -> value: " + entry.getKey() + ", expire: " + new DateTime(expire) + ", now: " + new DateTime(now));
									}
								}
							}
							if (delete) {
								jedis.publish(nodePath, Constants.UNREGISTER);
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void doRegister(Node node) {
		String key = NodeRegistryUtils.getNodeTypePath(nodeGroup, node.getConfig().getNodeType());
		String expire = String.valueOf(System.currentTimeMillis() + expirePeriod);
		boolean success = false;
		NodeRegistryException exception = null;
		for (Map.Entry<String, JedisPool> entry : jedisPoolMap.entrySet()) {
			JedisPool jedisPool = entry.getValue();
			try (Jedis jedis = jedisPool.getResource()) {
				jedis.hset(key, node.toFullString(), expire);
				jedis.publish(key, Constants.REGISTER);
				success = true;
				if (!replicate) {
					// 如果服务器端已同步数据，只需写入单台机器
					break;
				}
			} catch (Throwable t) {
				exception = new NodeRegistryException("Failed to register node to redis registry. registry: " + entry.getKey() + ", node: " + node + ", cause: " + t.getMessage(), t);
			}
		}
		if (null != exception) {
			if (success) {
				LOGGER.warn(MarkerFactory.getMarker(LogMarker.PLATFORM), exception.getMessage(), exception);
			} else {
				throw exception;
			}
		}
	}

	@Override
	protected void doUnRegister(Node node) {

	}

	@Override
	protected void doSubscribe(Node node, NotifyListener listener) {

	}

	@Override
	protected void doUnsubscribe(Node node, NotifyListener listener) {

	}

	private class Notifier extends Thread {


	}
}
