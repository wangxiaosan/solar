package com.wwy.common.lang.constants;

import java.util.regex.Pattern;

/**
 * @author wangxiaosan
 * @date 2017/10/20
 */
public interface Constants {
    // 可用的处理器个数
    int AVAILABLE_PROCESSOR = Runtime.getRuntime().availableProcessors();

    int DEFAULT_PROCESSOR_THREAD = 5;

    String OS_NAME = System.getProperty("os.name");

    String USER_HOME = System.getProperty("user.home");

    String LINE_SEPARATOR = System.getProperty("line.separator");

    // 默认集群名字
    String DEFAULT_CLUSTER_NAME = "defaultCluster";

    String CHARSET = "UTF-8";

    int DEFAULT_TIMEOUT = 1000;

    int DEFAULT_SESSION_TIMEOUT = 60 * 1000;

    String REGISTER = "register";

    String UNREGISTER = "unregister";

    String SUBSCRIBE = "subscribe";

    int DEFAULT_BUFFER_SIZE = 16 * 1024 * 1024;
    /**
     * 重试周期
     */
    int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;

    Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    int DEFAULT_REGISTRY_RECONNECT_PERIOD = 3 * 1000;

    String LOADBALANCE = "loadbalance";

    String REQUEST_HEADER_HOST = "Host";
    String REQUEST_HEADER_REMOTE_HOST = "Remote-Host";
    String REQUEST_HEADER_ACCESS_TOKEN = "accessToken";
    String REQUEST_HEADER_ACCESS_TOKEN_REMOVE = "accessTokenRemove";

    String UNKNOWN_ERROR = "UnknownError";
}
