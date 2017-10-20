package com.wwy.eureka.api.cluster;

import com.wwy.common.lang.constants.EventTopic;
import com.wwy.common.lang.constants.LogMarker;
import com.wwy.common.lang.utils.CollectionUtils;
import com.wwy.common.lang.utils.ConcurrentHashSet;
import com.wwy.common.lang.utils.ListUtils;
import com.wwy.eureka.api.event.EventInfo;
import com.wwy.eureka.api.listener.NodeChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangxiaosan
 * @date 2017/10/20
 *
 * 节点管理（自己关注的节点）
 */
public class SubscribedNodeManager implements NodeChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribedNodeManager.class);
    private final ConcurrentHashMap<NodeType, Set<Node>> NODES = new ConcurrentHashMap<NodeType, Set<Node>>();

    private Node currentNode;

    public SubscribedNodeManager(Node currentNode) {
        this.currentNode = currentNode;
    }

    /**
     * 添加监听的节点
     */
    private void addNode(Node node) {
        _addNode(node);
//        if ((NodeType.JOB_TRACKER.equals(node.getNodeType()))) {
//            // 如果增加的JobTracker节点，那么直接添加，因为所有节点都需要监听
//            _addNode(node);
//        } else if (NodeType.JOB_TRACKER.equals(appContext.getConfig().getNodeType())) {
//            // 如果当天节点是JobTracker节点，那么直接添加，因为JobTracker节点要监听三种节点
//            _addNode(node);
//        } else if (appContext.getConfig().getNodeType().equals(node.getNodeType())
//                && appContext.getConfig().getNodeGroup().equals(node.getGroup())) {
//            // 剩下这种情况是JobClient和TaskTracker都只监听和自己同一个group的节点
//            _addNode(node);
//        }
    }

    private void _addNode(Node node) {
        Set<Node> nodeSet = NODES.get(node.getConfig().getNodeType());
        if (CollectionUtils.isEmpty(nodeSet)) {
            nodeSet = new ConcurrentHashSet<Node>();
            Set<Node> oldNodeList = NODES.putIfAbsent(node.getConfig().getNodeType(), nodeSet);
            if (oldNodeList != null) {
                nodeSet = oldNodeList;
            }
        }
        nodeSet.add(node);
        EventInfo eventInfo = new EventInfo(EventTopic.NODE_ADD);
        eventInfo.setParam("node", node);
        currentNode.getEventCenter().publishSync(eventInfo);
        LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Add {}", node);
    }

    public List<Node> getNodeList(final NodeType nodeType, final String nodeGroup) {

        Set<Node> nodes = NODES.get(nodeType);

        return ListUtils.filter(CollectionUtils.setToList(nodes), new ListUtils.Filter<Node>() {
            @Override
            public boolean filter(Node node) {
                return node.getConfig().getNodeGroup().equals(nodeGroup);
            }
        });
    }

    public List<Node> getNodeList(NodeType nodeType) {
        return CollectionUtils.setToList(NODES.get(nodeType));
    }

    public Node getNode(String identity) {
        for (Set<Node> nodes : NODES.values()) {
            for (Node node : nodes) {
                if (identity.equals(node.getConfig().getIdentity())) {
                    return node;
                }
            }
        }
        return null;
    }

    private void removeNode(Node delNode) {
        Set<Node> nodeSet = NODES.get(delNode.getConfig().getNodeType());

        if (CollectionUtils.isNotEmpty(nodeSet)) {
            for (Node node : nodeSet) {
                if (node.getConfig().getIdentity().equals(delNode.getConfig().getIdentity())) {
                    nodeSet.remove(node);
                    EventInfo eventInfo = new EventInfo(EventTopic.NODE_REMOVE);
                    eventInfo.setParam("node", node);
                    currentNode.getEventCenter().publishSync(eventInfo);
                    LOGGER.info(MarkerFactory.getMarker(LogMarker.PLATFORM), "Remove {}", node);
                }
            }
        }
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            addNode(node);
        }
    }

    @Override
    public void removeNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            removeNode(node);
        }
    }
}
