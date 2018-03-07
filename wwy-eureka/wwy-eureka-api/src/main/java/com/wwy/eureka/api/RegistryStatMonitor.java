package com.wwy.eureka.api;

import com.wwy.common.lang.constants.EventTopic;
import com.wwy.eureka.api.cluster.Node;
import com.wwy.eureka.api.event.EventInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wangxiaosan
 * @date 2018/03/07
 */
public class RegistryStatMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryStatMonitor.class);
    private Node node;
    private AtomicBoolean available = new AtomicBoolean(false);

    public RegistryStatMonitor(Node appContext) {
        this.node = appContext;
    }

    public void setAvailable(boolean available) {
        this.available.set(available);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Registry {}", available ? "available" : "unavailable");
        }
        // 发布事件
        node.getEventCenter().publishAsync(new EventInfo(
                available ? EventTopic.REGISTRY_AVAILABLE : EventTopic.REGISTRY_UN_AVAILABLE));
    }

    public boolean isAvailable() {
        return this.available.get();
    }
}
