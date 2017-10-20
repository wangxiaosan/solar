package com.wwy.eureka.api.event;

/**
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
}
