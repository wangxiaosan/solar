package com.wwy.eureka.api.cluster;

import com.wwy.eureka.api.event.EventCenter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangxiaosan
 * @date 2017/10/19
 *
 * 服务节点
 */
public class Node {

    private NodeConfig config;

    private Long createTime;

    private String hostName;

    // 自己关注的节点类型
    private List<NodeType> listenNodeTypes;

    private String fullString;

    // 节点管理
    private SubscribedNodeManager subscribedNodeManager;
    // master节点选举者
    private MasterElector masterElector;
    // 事件中心
    private EventCenter eventCenter;
    // 注册中心状态监控
//    private RegistryStatMonitor registryStatMonitor;

    public EventCenter getEventCenter() {
        return eventCenter;
    }

    public void setEventCenter(EventCenter eventCenter) {
        this.eventCenter = eventCenter;
    }

    public SubscribedNodeManager getSubscribedNodeManager() {
        return subscribedNodeManager;
    }

    public void setSubscribedNodeManager(SubscribedNodeManager subscribedNodeManager) {
        this.subscribedNodeManager = subscribedNodeManager;
    }

    public MasterElector getMasterElector() {
        return masterElector;
    }

    public void setMasterElector(MasterElector masterElector) {
        this.masterElector = masterElector;
    }

//    public RegistryStatMonitor getRegistryStatMonitor() {
//        return registryStatMonitor;
//    }

//    public void setRegistryStatMonitor(RegistryStatMonitor registryStatMonitor) {
//        this.registryStatMonitor = registryStatMonitor;
//    }


    public NodeConfig getConfig() {
        return config;
    }

    public void setConfig(NodeConfig config) {
        this.config = config;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public List<NodeType> getListenNodeTypes() {
        return listenNodeTypes;
    }

    public void setListenNodeTypes(List<NodeType> listenNodeTypes) {
        this.listenNodeTypes = listenNodeTypes;
    }

    public void addListenNodeType(NodeType nodeType) {
        if (this.listenNodeTypes == null) {
            this.listenNodeTypes = new ArrayList<NodeType>();
        }
        this.listenNodeTypes.add(nodeType);
    }

    /**
     * 启动节点
     */
    public void start() {

    }

    /**
     * 停止节点
     */
    public void stop() {

    }

    /**
     * destroy
     */
    public void destroy() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return !(config.getIdentity() != null ? !config.getIdentity().equals(node.getConfig().getIdentity()) : node.getConfig().getIdentity() != null);

    }

    @Override
    public int hashCode() {
        return config.getIdentity() != null ? config.getIdentity().hashCode() : 0;
    }

    public String getAddress() {
        return config.getIp() + ":" + config.getListenPort();
    }

    public String toFullString() {
        if (fullString == null) {
//            fullString = NodeRegistryUtils.getFullPath(this);
        }
        return fullString;
    }

    @Override
    public String toString() {
        return toFullString();
    }
}
