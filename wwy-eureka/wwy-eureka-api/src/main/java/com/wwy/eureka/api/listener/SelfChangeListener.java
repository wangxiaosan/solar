package com.wwy.eureka.api.listener;

import com.wwy.common.lang.constants.EventTopic;
import com.wwy.common.lang.utils.CollectionUtils;
import com.wwy.eureka.api.cluster.Node;
import com.wwy.eureka.api.cluster.NodeConfig;
import com.wwy.eureka.api.cluster.NodeType;
import com.wwy.eureka.api.event.EventInfo;

import java.util.List;

/**
 * @author wangxiaosan
 * @date 2017/10/27
 *  本节点的监听
 */
public class SelfChangeListener implements  NodeChangeListener {

    private NodeConfig config;
    private Node node;

    public SelfChangeListener(Node node) {
        this.config = node.getConfig();
        this.node = node;
    }

    private void change(Node node) {
        if (node.getConfig().getIdentity().equals(config.getIdentity())) {
            // 是当前节点, 看看节点配置是否发生变化
            // 1. 看 threads 有没有改变 , 目前只有 TASK_TRACKER 对 threads起作用
            if (node.getConfig().getNodeType().equals(NodeType.TASK_TRACKER)
                    && (node.getConfig().getWorkThreads() != config.getWorkThreads())) {
                config.setWorkThreads(node.getConfig().getWorkThreads());
                this.node.getEventCenter().publishAsync(new EventInfo(EventTopic.WORK_THREAD_CHANGE));
            }

            // 2. 看 available 有没有改变
            if (node.getConfig().isAvailable() != config.isAvailable()) {
                String topic = node.getConfig().isAvailable() ? EventTopic.NODE_ENABLE : EventTopic.NODE_DISABLE;
                config.setAvailable(node.getConfig().isAvailable());
                this.node.getEventCenter().publishAsync(new EventInfo(topic));
            }
        }
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            change(node);
        }
    }

    @Override
    public void removeNodes(List<Node> nodes) {

    }
}
