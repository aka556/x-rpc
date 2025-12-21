package org.xiaoyu.common.message;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageType {
    // 枚举常量，消息请求
    REQUEST(0),
    // 消息响应
    RESPONSE(1);

    private int code;

    public int getCode() {
        return code;
    }
}
