package com.wwy.eureka.api.cluster;

import org.springframework.util.StringUtils;

/**
 * @author wangxiaosan
 * @date 2017/10/19
 */
public enum NodeType {
    // job tracker
    JOB_TRACKER,
    // task tracker
    TASK_TRACKER,
    // client
    JOB_CLIENT,
    // monitor
    MONITOR,

    BACKEND;

    public static NodeType convert(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return NodeType.valueOf(value);
    }
}
