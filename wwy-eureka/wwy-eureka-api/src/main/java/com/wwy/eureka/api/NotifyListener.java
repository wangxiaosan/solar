package com.wwy.eureka.api;

import com.wwy.eureka.api.cluster.Node;

import java.util.List;

/**
 * @author wangxiaosan
 * @date 2017/10/19
 */
public interface NotifyListener {
    void notify(NotifyEvent event, List<Node> nodes);
}
