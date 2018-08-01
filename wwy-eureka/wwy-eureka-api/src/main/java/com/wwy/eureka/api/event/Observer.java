package com.wwy.eureka.api.event;

/**
 * @author wangxiaosan
 * @date 2017/10/20
 *
 * 事件观察者接口
 */
public interface Observer {
	/**
	 * 观察
	 * @param eventInfo 事件信息
	 */
    void onObserved(EventInfo eventInfo);
}
