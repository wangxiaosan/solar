package com.wwy.eureka.api.event;

import com.wwy.common.lang.spi.SPI;

/**
 * @author wangxiaosan
 * @date 2017/10/20
 *
 * 事件中心接口
 */

@SPI("injvm")
public interface EventCenter {
	/**
	 * 订阅主题
	 * @param subscriber 订阅者
	 * @param topics 订阅主题
	 */
	void subscribe(EventSubscriber subscriber, String... topics);

	/**
	 * 取消订阅主题
	 * @param topic 主题
	 * @param subscriber 订阅者
	 */
	void unSubscribe(String topic, EventSubscriber subscriber);

	/**
	 * 同步发布主题消息
	 * @param eventInfo 事件消息
	 */
	void publishSync(EventInfo eventInfo);

	/**
	 * 异步发送主题消息
	 * @param eventInfo 事件消息
	 */
	void publishAsync(EventInfo eventInfo);
}
