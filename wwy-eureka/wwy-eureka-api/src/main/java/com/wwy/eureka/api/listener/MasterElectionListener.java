package com.wwy.eureka.api.listener;

import com.wwy.common.lang.utils.CollectionUtils;
import com.wwy.eureka.api.cluster.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangxiaosan
 * @date 2017/10/27
 *
 * 监听自己类型的节点变化，选举mater
 */
public class MasterElectionListener implements NodeChangeListener {

    private Node node;

    public MasterElectionListener(Node node) {
        this.node = node;
    }

    @Override
    public void removeNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        // 只需要和当前节点相同的节点类型和组
        List<Node> groupNodes = new ArrayList<Node>();
        for (Node node : nodes) {
            if (isSameGroup(node)) {
                groupNodes.add(node);
            }
        }
        if (groupNodes.size() > 0) {
            node.getMasterElector().removeNode(groupNodes);
        }
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        // 只需要和当前节点相同的节点类型和组
        List<Node> groupNodes = new ArrayList<Node>();
        for (Node node : nodes) {
            if (isSameGroup(node)) {
                groupNodes.add(node);
            }
        }
        if (groupNodes.size() > 0) {
            node.getMasterElector().addNodes(groupNodes);
        }
    }

    /**
     * 是否和当前节点是相同的GROUP
     *
     * @param node
     * @return
     */
    private boolean isSameGroup(Node node) {
        return node.getConfig().getNodeType().equals(this.node.getConfig().getNodeType())
                && node.getConfig().getNodeGroup().equals(this.node.getConfig().getNodeGroup());
    }

}
