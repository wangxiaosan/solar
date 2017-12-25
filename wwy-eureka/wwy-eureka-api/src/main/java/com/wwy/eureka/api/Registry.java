package com.wwy.eureka.api;

import com.wwy.common.lang.spi.SPI;
import com.wwy.eureka.api.cluster.Node;

/**
 * @author wangxiaosan
 * @date 2017/10/19
 *
 * 节点注册接口
 */
@SPI("zookeeper")
public interface Registry {
    /**
     * 节点注册
     */
    void register(Node node);

    /**
     * 节点 取消注册
     */
    void unregister(Node node);

    /**
     * 监听节点
     */
    void subscribe(Node node, NotifyListener listener);

    /**
     * 取消监听节点
     */
    void unsubscribe(Node node, NotifyListener listener);

    /**
     * 销毁
     */
    void destroy();
}
