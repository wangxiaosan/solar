package com.wwy.eureka.api;

import com.wwy.common.lang.constants.EventTopic;
import com.wwy.eureka.api.cluster.Node;
import com.wwy.eureka.api.event.EventInfo;
import com.wwy.eureka.api.event.EventSubscriber;
import com.wwy.eureka.api.event.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * @author wangxiaosan
 * @date 2018/03/07
 */
public class NodeShutdownHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeShutdownHook.class);

    public static void registerHook(Node node, final String name, final Callable callback) {
        node.getEventCenter().subscribe(new EventSubscriber(name + "_" + node.getConfig().getIdentity(), new Observer() {
            @Override
            public void onObserved(EventInfo eventInfo) {
                if (callback != null) {
                    try {
                        callback.call();
                    } catch (Exception e) {
                        LOGGER.warn("Call shutdown hook {} error", name, e);
                    }
                }
            }
        }), EventTopic.NODE_SHUT_DOWN);
    }
}
