package com.wwy.eureka.multicast;

import com.wwy.common.lang.NamedThreadFactory;
import com.wwy.common.lang.constants.Constants;
import com.wwy.common.lang.constants.LogMarker;
import com.wwy.common.lang.utils.ConcurrentHashSet;
import com.wwy.eureka.api.FailbackRegistry;
import com.wwy.eureka.api.NodeRegistryUtils;
import com.wwy.eureka.api.NotifyEvent;
import com.wwy.eureka.api.NotifyListener;
import com.wwy.eureka.api.cluster.Node;
import com.wwy.eureka.api.cluster.NodeConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MarkerFactory;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author wangxiaosan
 * @date 2018/03/07
 */
public class MulticastRegistry extends FailbackRegistry {
    private static final String DEFAULT_MULTICAST_ADDRESS = "230.30.1.1";
    private static final int DEFAULT_MULTICAST_PORT = 1234;
    public static final int DEFAULT_SESSION_TIMEOUT = 60 * 1000;

    private InetAddress mutilcastAddress;

    private MulticastSocket mutilcastSocket;

    private int mutilcastPort;

    private ConcurrentMap<Node, Set<Node>> received = new ConcurrentHashMap<>();

    private ScheduledExecutorService cleanExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("MulticastRegistryCleanTimer", true));

    private int cleanPeriod;

    public MulticastRegistry(Node currentNode) {
        super(currentNode);
        try {
            mutilcastAddress = InetAddress.getByName(DEFAULT_MULTICAST_ADDRESS);
            mutilcastPort = DEFAULT_MULTICAST_PORT;

            NodeConfig config = currentNode.getConfig();
            String address = NodeRegistryUtils.getRealRegistryAddress(config.getRegistryAddress());
            if (StringUtils.isNotBlank(address)) {
                int i = address.indexOf(':');
                String host = address.substring(0, i);
                mutilcastAddress = InetAddress.getByName(host);
                mutilcastPort = Integer.parseInt(address.substring(i + 1));
            }
            mutilcastSocket = new MulticastSocket(mutilcastPort);
            mutilcastSocket.setLoopbackMode(false);
            mutilcastSocket.joinGroup(mutilcastAddress);

            Thread thread = new Thread(new Runnable() {
                public void run() {
                    byte[] buf = new byte[2048];
                    DatagramPacket recv = new DatagramPacket(buf, buf.length);
                    while (!mutilcastSocket.isClosed()) {
                        try {
                            mutilcastSocket.receive(recv);
                            String msg = new String(recv.getData()).trim();
                            int i = msg.indexOf('\n');
                            if (i > 0) {
                                msg = msg.substring(0, i).trim();
                            }
                            receive(msg, (InetSocketAddress) recv.getSocketAddress());
                            Arrays.fill(buf, (byte) 0);
                        } catch (Throwable e) {
                            if (!mutilcastSocket.isClosed()) {
                                LOGGER.error(MarkerFactory.getMarker(LogMarker.PLATFORM), e.getMessage(), e);
                            }
                        }
                    }
                }
            }, "MulticastRegistryReceiver");
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(MarkerFactory.getMarker(LogMarker.PLATFORM), e.getMessage(), e);
        }

        this.cleanPeriod = DEFAULT_SESSION_TIMEOUT;
        cleanExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    clean(); // 清除过期者
                } catch (Throwable t) { // 防御性容错
                    LOGGER.error(MarkerFactory.getMarker(LogMarker.PLATFORM), "Unexpected exception occur at clean expired node, cause: " + t.getMessage(), t);
                }
            }
        }, cleanPeriod, cleanPeriod, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doRegister(Node node) {
        broadcast(Constants.REGISTER + " " + NodeRegistryUtils.getFullPath(node));
    }

    @Override
    protected void doUnRegister(Node node) {
        broadcast(Constants.UNREGISTER + " " + NodeRegistryUtils.getFullPath(node));
    }

    @Override
    protected void doSubscribe(Node node, NotifyListener listener) {
        broadcast(Constants.SUBSCRIBE + " " + NodeRegistryUtils.getFullPath(node));
    }

    @Override
    protected void doUnsubscribe(Node node, NotifyListener listener) {

    }

    private void clean() {
        for (Set<Node> providers : new HashSet<>(received.values())) {
            for (Node node : new HashSet<>(providers)) {
                if (NodeRegistryUtils.isExpired(node.getConfig())) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(MarkerFactory.getMarker(LogMarker.PLATFORM), "Clean expired node " + node);
                    }
                    doUnRegister(node);
                }
            }
        }
    }

    private void receive(String msg, InetSocketAddress remoteAddress) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Receive multicast message: " + msg + " from " + remoteAddress);
        }
        if (msg.startsWith(Constants.REGISTER)) {
            Node node = NodeRegistryUtils.parse(msg.substring(Constants.REGISTER.length()).trim());
            for (Map.Entry<Node, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
                Node key = entry.getKey();
                Set<Node> nodes = received.get(key);
                if (null == nodes) {
                    received.putIfAbsent(key, new ConcurrentHashSet<>());
                    nodes = received.get(key);
                }
                nodes.add(node);
                for (NotifyListener listener : entry.getValue()) {
                    notify(NotifyEvent.ADD, new ArrayList<>(nodes), listener);
                }

            }
        } else if (msg.startsWith(Constants.UNREGISTER)) {
            Node node = NodeRegistryUtils.parse(msg.substring(Constants.UNREGISTER.length()).trim());
            for (Map.Entry<Node, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
                Node key = entry.getKey();
                Set<Node> nodes = received.get(key);
                if (null != nodes) {
                    nodes.remove(node);
                }
                for (NotifyListener listener : entry.getValue()) {
                    ArrayList<Node> nodeList = new ArrayList<>();
                    nodeList.add(node);
                    notify(NotifyEvent.REMOVE, nodeList, listener);
                }

            }
        } else if (msg.startsWith(Constants.SUBSCRIBE)) {
            Node node = NodeRegistryUtils.parse(msg.substring(Constants.SUBSCRIBE.length()).trim());
//            Set<Node> registered = getRegistered();
//            if (registered != null && registered.size() > 0) {
//                for (Node u : registered) {
            if (!currentNode.toFullString().equals(node.toFullString())) {
                broadcast(Constants.REGISTER + " " + currentNode.toFullString());
            }
//                }
//            }
        }/* else if (msg.startsWith(UNSUBSCRIBE)) {
        }*/
    }

    private void broadcast(String msg) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Send broadcast message: " + msg + " to " + mutilcastAddress + ":" + mutilcastPort);
        }
        try {
            byte[] data = (msg + "\n").getBytes();
            DatagramPacket hi = new DatagramPacket(data, data.length, mutilcastAddress, mutilcastPort);
            mutilcastSocket.send(hi);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void unicast(String msg, String host) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Send unicast message: " + msg + " to " + host + ":" + mutilcastPort);
        }
        try {
            byte[] data = (msg + "\n").getBytes();
            DatagramPacket hi = new DatagramPacket(data, data.length, InetAddress.getByName(host), mutilcastPort);
            mutilcastSocket.send(hi);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
