package org.xiaoyu.common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcResponse implements Serializable {
    private int code; // 响应码
    private String message; // 响应信息
    // 加入传输数据的类型，以便在自定义序列化器中解析
    private Class<?> dataType;
    private Object data; // 响应数据

    public static RpcResponse success(Object data) {
        return RpcResponse.builder().code(200).dataType(data.getClass()).data(data).build();
    }

    // 动态传入错误信息
    public static RpcResponse fail(String msg) {
        return RpcResponse.builder().code(500).message(msg).build();
    }
}
