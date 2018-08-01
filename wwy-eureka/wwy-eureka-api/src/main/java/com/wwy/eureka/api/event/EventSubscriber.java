package com.wwy.eureka.api.event;

import com.google.common.base.Objects;

/**
 * 事件订阅者
 * @author wangxiaosan
 * @date 2017/10/20
 */
public class EventSubscriber {

    private String id;
    private Observer observer;

    public EventSubscriber(String id, Observer observer) {
        this.id = id;
        this.observer = observer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Observer getObserver() {
        return observer;
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		EventSubscriber that = (EventSubscriber) o;
		return Objects.equal(id, that.id) &&
				Objects.equal(observer, that.observer);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, observer);
	}
}
