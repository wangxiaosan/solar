package com.wwy.eureka.api.listener;

import com.wwy.eureka.api.cluster.Node;

/**
 * @author wangxiaosan
 * @date 2017/10/20
 *
 * master节点变化监听器
 */
public interface MasterChangeListener {

    /**
     * @param master master 节点
     * @param isMaster 当前节点是不是master节点
     */
    void change(Node master, boolean isMaster);
}
