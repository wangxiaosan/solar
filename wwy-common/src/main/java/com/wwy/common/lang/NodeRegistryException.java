package com.wwy.common.lang;

/**
 * @author wangxiaosan
 * @date 2018/03/07
 */
public class NodeRegistryException extends RuntimeException {

    public NodeRegistryException() {
        super();
    }

    public NodeRegistryException(String message) {
        super(message);
    }

    public NodeRegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeRegistryException(Throwable cause) {
        super(cause);
    }

}
