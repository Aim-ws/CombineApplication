package com.ws.message.bus;

import androidx.annotation.Nullable;

/**
 * Create time 2023/11/9 9:33
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
class MessageEvent {
    private String action;
    private Object event;

    public MessageEvent() {
    }

    public String getAction() {
        return action == null ? "" : action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Nullable
    public Object getEvent() {
        return event;
    }

    public void setEvent(Object event) {
        this.event = event;
    }
}
