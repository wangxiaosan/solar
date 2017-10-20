package com.wwy.eureka.api.listener;

import com.wwy.eureka.api.cluster.Node;

import java.util.List;

/**
 * @author wangxiaosan
 * @date 2017/10/20
 */
public interface NodeChangeListener {

    /**
     * 添加节点
     * @param nodes
     */
    void addNodes(List<Node> nodes);

    /**
     * 删除节点
     * @param nodes
     */
    void removeNodes(List<Node> nodes);
}
