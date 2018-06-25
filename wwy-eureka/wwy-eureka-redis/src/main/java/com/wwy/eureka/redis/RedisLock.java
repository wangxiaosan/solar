package com.wwy.eureka.redis;

import lombok.Data;
import redis.clients.jedis.Jedis;

/**
 * @Author:weiyang.wang
 * @Date: 2018/6/25
 *
 */
@Data
public class RedisLock {
	private String lockKey;
	private String lockValue;
	private int expiredSeconds;

	public RedisLock(String lockKey, String lockValue, int expiredSeconds) {
		this.lockKey = lockKey;
		this.lockValue = lockValue;
		this.expiredSeconds = expiredSeconds;
	}

	public boolean acquire(Jedis jedis) {
		String value = jedis.get(lockKey);
		if(value == null) {
			boolean success = jedis.setnx(lockKey, lockValue) == 1;
			if(success) {
				jedis.expire(lockKey, expiredSeconds);
				return true;
			}
		} else if(lockValue.equals(value)) {
			jedis.expire(lockKey, expiredSeconds);
			return true;
		}
		return false;
	}
}
