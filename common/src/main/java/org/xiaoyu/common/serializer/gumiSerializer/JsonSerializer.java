package org.xiaoyu.common.serializer.gumiSerializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.xiaoyu.common.message.RpcRequest;
import org.xiaoyu.common.message.RpcResponse;

public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        // 转换为json格式的字节数组
        byte[] bytes = JSONObject.toJSONBytes(obj);
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object object = null;
        switch (messageType) {
            case 0:
                // 将字节数组转化为RpcRequest对象
                RpcRequest request = JSON.parseObject(bytes, RpcRequest.class);
                // 创建一个 Object 类型的数组，用于存储解析后的请求参数
                Object[] objects = new Object[request.getParams().length];
                // 对转化后的request的params属性进行类型判断
                for (int i = 0; i < objects.length; i++) {
                    Class<?> paramsType = request.getParamsType()[i];

                    //如果类型兼容，则直接赋值，否则使用fastjson进行类型转换
                    if (!paramsType.isAssignableFrom(request.getParams()[i].getClass())) {
                        objects[i] = JSONObject.toJavaObject((JSONObject) request.getParams()[i], request.getParamsType()[i]);
                    } else {
                        objects[i] = request.getParams()[i];
                    }
                }
                request.setParams(objects);
                object = request;
                break;
            case 1:
                // 将字节对象用户转为RpcResponse对象
                RpcResponse response = JSON.parseObject(bytes, RpcResponse.class);
                // 数据类型
                Class<?> dataType = response.getDataType();
                if (!dataType.isAssignableFrom(response.getData().getClass())) {
                    response.setData(JSONObject.toJavaObject((JSONObject) response.getData(), dataType));
                }
                object = response;
                break;
            default:
                System.out.println("暂不支持该消息类型");
                throw new RuntimeException();
        }
        return object;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public String toString() {
        return "JsonSerializer";
    }
}
