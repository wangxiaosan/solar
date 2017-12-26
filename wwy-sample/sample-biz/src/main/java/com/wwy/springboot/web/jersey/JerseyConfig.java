package com.wwy.springboot.web.jersey;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

/**
 * @author wangxiaosan
 * @date 2017/10/17
 */
public class JerseyConfig extends ResourceConfig{
    public JerseyConfig() {
        register(RequestContextFilter.class);
        //配置restful package.
        packages("com.wwy.springboot.biz.action");
    }
}
