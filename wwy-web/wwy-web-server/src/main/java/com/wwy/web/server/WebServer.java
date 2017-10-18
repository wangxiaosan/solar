package com.wwy.web.server;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.Map;

/**
 * @author wangxiaosan
 * @date 2017/10/17
 */
public interface WebServer {

    void start() throws Exception;

    void stop() throws Exception;

    void addContextInitParam(String name,String value);

    void addServlet(Servlet servlet, String pathSpec, Map<String, String> initParams);

    void addFilter(Filter filter, String pathSpec, Map<String, String> initParams, EnumSet<DispatcherType> dispatcherTypes);

    void addEventListener(EventListener listener);

    String getServerHost();

    int getServerPort();
}
