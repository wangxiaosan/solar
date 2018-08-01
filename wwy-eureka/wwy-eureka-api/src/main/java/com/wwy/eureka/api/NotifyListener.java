package com.wwy.eureka.api;

import com.wwy.eureka.api.cluster.Node;

import java.util.List;

/**
 * 监听通知
 * @author wangxiaosan
 * @date 2017/10/19
 */
public interface NotifyListener {
	/**
	 * 通知
	 * @param event 通知事件
	 * @param nodes 节点
	 */
    void notify(NotifyEvent event, List<Node> nodes);
}
