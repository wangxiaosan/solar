package com.wwy.web.server;

import com.wwy.eureka.api.cluster.Node;
import org.springframework.context.ApplicationContext;

/**
 * @author wangxiaosan
 * @date 2017/10/17
 */
public interface WorkStatusListener {

    void preStartWorker(WebServer webServer, ApplicationContext ctx);

    void postStartWorker(WebServer webServer, ApplicationContext ctx, Node node);
}
