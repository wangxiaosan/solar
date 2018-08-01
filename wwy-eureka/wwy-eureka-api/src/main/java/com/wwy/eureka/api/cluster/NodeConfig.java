package com.wwy.eureka.api.cluster;

import com.alibaba.fastjson.JSON;

/**
 * @author wangxiaosan
 * @date 2017/10/19
 *
 * 服务节点配置
 *
 */
public class NodeConfig {
    private String name;

	/**
	 * 节点是否可用
	 */
	private boolean available = true;

	/**
	 * 应用节点组
	 */
	private String nodeGroup;
	/**
	 * 唯一标识
	 */
	private String identity;
	/**
	 * 工作线程, 目前只对 TaskTracker 有效
	 */
	private int workThreads;
	/**
	 * 节点类型
	 */
	private NodeType nodeType;
	/**
	 * 注册中心 地址
	 */
	private String registryAddress;
	/**
	 * 远程连接超时时间
	 */
	private int invokeTimeoutMillis;
	/**
	 * 监听端口
	 */
	private int listenPort;

    private int serverPort;

    private String ip;
	/**
	 *  任务信息存储路径(譬如TaskTracker反馈任务信息给JobTracker, JobTracker down掉了, 那么存储下来等待JobTracker可用时再发送)
	 */
	private String dataPath;
	/**
	 *  集群名字
	 */
	private String clusterName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public int getWorkThreads() {
        return workThreads;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public int getInvokeTimeoutMillis() {
        return invokeTimeoutMillis;
    }

    public void setInvokeTimeoutMillis(int invokeTimeoutMillis) {
        this.invokeTimeoutMillis = invokeTimeoutMillis;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
