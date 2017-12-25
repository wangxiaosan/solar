package com.wwy.eureka.api;

import com.wwy.common.lang.constants.LogMarker;
import com.wwy.common.lang.utils.CollectionUtils;
import com.wwy.common.lang.utils.ConcurrentHashSet;
import com.wwy.eureka.api.cluster.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author wangxiaosan
 * @date 2017/10/31
 */
public abstract class AbstractRegistry implements Registry {

    protected final static Logger LOGGER = LoggerFactory.getLogger(Registry.class);

    private final Set<Node> registered = new ConcurrentHashSet<>();
    private final ConcurrentMap<Node, Set<NotifyListener>> subscribed = new ConcurrentHashMap<>();

    protected final Node currentNode;

    public AbstractRegistry(Node currentNode) {
        this.currentNode = currentNode;
    }

    @Override
    public void register(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("register node == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Register: " + node);
        }
        registered.add(node);
    }

    @Override
    public void unregister(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("unregister node == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Unregister: " + node);
        }
        registered.remove(node);
    }

    @Override
    public void subscribe(Node node, NotifyListener listener) {
        if (node == null) {
            throw new IllegalArgumentException("subscribe node == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Subscribe: " + node);
        }
        Set<NotifyListener> listeners = subscribed.get(node);
        if (listeners == null) {
            subscribed.putIfAbsent(node, new ConcurrentHashSet<>());
            listeners = subscribed.get(node);
        }
        listeners.add(listener);

    }

    @Override
    public void unsubscribe(Node node, NotifyListener listener) {
        if (node == null) {
            throw new IllegalArgumentException("unsubscribe node == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("unsubscribe listener == null");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "unsubscribe: " + node);
        }
        Set<NotifyListener> listeners = subscribed.get(node);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    protected void notify(NotifyEvent event, List<Node> nodes, NotifyListener listener) {
        if (event == null) {
            throw new IllegalArgumentException("notify event == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        if (CollectionUtils.isEmpty(nodes)) {
            LOGGER.warn(MarkerFactory.getMarker(LogMarker.PLATFORM), "Ignore empty notify nodes for subscribe node " + currentNode);
            return;
        }

        listener.notify(event, nodes);
    }

    @Override
    public void destroy() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Destroy registry:" + currentNode);
        }
        Set<Node> destroyRegistered = new HashSet<>(getRegistered());
        if (!destroyRegistered.isEmpty()) {
            for (Node node : new HashSet<>(getRegistered())) {
                try {
                    unregister(node);
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Destroy unregister node " + node);
                    }
                } catch (Throwable t) {
                    LOGGER.warn(MarkerFactory.getMarker(LogMarker.PLATFORM), "Failed to unregister node " + node + " to registry " + currentNode + " on destroy, cause: " + t.getMessage(), t);
                }
            }
        }
        Map<Node, Set<NotifyListener>> destroySubscribed = new HashMap<>(getSubscribed());
        if (!destroySubscribed.isEmpty()) {
            for (Map.Entry<Node, Set<NotifyListener>> entry : destroySubscribed.entrySet()) {
                Node node = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    try {
                        unsubscribe(node, listener);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Destroy unsubscribe node " + node);
                        }
                    } catch (Throwable t) {
                        LOGGER.warn(MarkerFactory.getMarker(LogMarker.PLATFORM), "Failed to unsubscribe node " + node + " to registry " + currentNode + " on destroy, cause: " + t.getMessage(), t);
                    }
                }
            }
        }
    }

    protected Set<Node> getRegistered() {
        return registered;
    }

    protected ConcurrentMap<Node, Set<NotifyListener>> getSubscribed() {
        return subscribed;
    }

    /**
     * 恢复
     *
     * @throws Exception
     */
    protected void recover() throws Exception {
        // register
        Set<Node> recoverRegistered = new HashSet<>(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Recover register node " + recoverRegistered);
            }
            for (Node node : recoverRegistered) {
                register(node);
            }
        }
        // subscribe
        Map<Node, Set<NotifyListener>> recoverSubscribed = new HashMap<>(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Recover subscribe node " + recoverSubscribed.keySet());
            }
            for (Map.Entry<Node, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                Node node = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    subscribe(node, listener);
                }
            }
        }
    }
}
