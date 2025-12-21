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
public class RpcRequest implements Serializable {
    private String interfaceName; // 接口名
    private String methodName; // 方法名
    private Object[] params; // 参数对象序列
    private Class<?>[] paramsType; // 参数类型
}
