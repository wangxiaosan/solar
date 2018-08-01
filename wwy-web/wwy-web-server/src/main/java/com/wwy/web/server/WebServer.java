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

	/**
	 * web服务启动
	 * @throws Exception
	 */
    void start() throws Exception;

	/**
	 * web服务停止
	 * @throws Exception
	 */
	void stop() throws Exception;

	/**
	 * 增加Servlet Context 初始化key-value
	 * @param name
	 * @param value
	 */
    void addContextInitParam(String name,String value);

	/**
	 * 增加servlet
	 * @param servlet
	 * @param pathSpec
	 * @param initParams
	 */
	void addServlet(Servlet servlet, String pathSpec, Map<String, String> initParams);

	/**
	 * 增加feilter
	 * @param filter
	 * @param pathSpec
	 * @param initParams
	 * @param dispatcherTypes
	 */
    void addFilter(Filter filter, String pathSpec, Map<String, String> initParams, EnumSet<DispatcherType> dispatcherTypes);

	/**
	 * 增加事件监听
	 * @param listener
	 */
	void addEventListener(EventListener listener);

	/**
	 * 获取server host
	 * @return
	 */
    String getServerHost();

	/**
	 * 获取server port
	 * @return
	 */
	int getServerPort();
}
