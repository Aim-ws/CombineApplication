package com.ws.message.bus;

import androidx.annotation.NonNull;

/**
 * Create time 2023/11/9 9:30
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
public class MessageBus {
    public static final String TAG = "MessageBus";
    private static volatile MessageBus bus;
    MessageEngine mMessageEngine;

    private MessageBus() {
        mMessageEngine = new MessageEngine();
    }

    public static MessageBus getDefault() {
        if (bus == null) {
            synchronized (MessageBus.class) {
                if (bus == null) {
                    bus = new MessageBus();
                }
            }
        }
        return bus;
    }

    /**
     * register MessageBus
     *
     * @param subscriber Object
     */
    public void register(Object subscriber) {
        mMessageEngine.register(subscriber);
    }

    public void unregister(Object subscriber) {
        mMessageEngine.unregister(subscriber);
    }

    public void post(@NonNull String action, Object event) {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setAction(action);
        messageEvent.setEvent(event);
        post(messageEvent);
    }

    public void post(@NonNull String action) {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setAction(action);
        post(messageEvent);
    }

    public void post(Object event) {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setEvent(event);
        post(messageEvent);
    }

    public void post() {
        post(new MessageEvent());
    }

    private void post(MessageEvent event) {
        mMessageEngine.post(event);
    }

}
