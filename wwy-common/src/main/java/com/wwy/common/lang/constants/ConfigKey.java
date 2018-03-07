package com.wwy.common.lang.constants;

/**
 * @author wangxiaosan
 * @date 2018/03/07
 */
public class ConfigKey {

    /**
     * 注册中心自动重连时间
     */
    public static final    String REGISTRY_RECONNECT_PERIOD_KEY = "registry.reconnect.period";
    /**
     * 注册中心失败事件重试事件
     */
    public static final  String REGISTRY_RETRY_PERIOD_KEY = "registry.retry.period";
    public static final   String REDIS_SESSION_TIMEOUT = "redis.session.timeout";


    public static final   String REGISTRY_ADDRESS = "worker.admin.registryAddress";
    /**
     * worker相關配置
     */
    public static final  String WORKER_GROUP = "worker.admin.group";
    public static final   String WORKER_IDENTITY = "worker.admin.identity";
    public static final   String WORKER_ADMIN_HOST = "worker.admin.host";

    public static final String WORKER_REMOTING_NETWORK_MANAGER = "worker.admin.remotingNetworkManager";
    public static final String WORKER_ADMIN_REGISTRY = "worker.admin.registry";

    public static final String WORKER_REMOTE_SERVICE_CLIENT = "worker.admin.remotingServiceClient";
}
