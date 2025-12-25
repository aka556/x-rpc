package org.xiaoyu.common.message;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RequestType {
    NORMAL(0),
    HEARTBEAT(1);

    private int code;

    public int getCode() {
        return code;
    }
}
