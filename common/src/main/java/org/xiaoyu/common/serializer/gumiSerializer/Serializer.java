package org.xiaoyu.common.serializer.gumiSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义序列化接口
 * 包含序列化与反序列化操作
 */
public interface Serializer {
    // 把对象序列化为字节数组
    byte[] serialize(Object obj);
    // 解序列化(反序列化)操作，根据字节数组和消息类型合成消息对象
    Object deserialize(byte[] bytes, int messageType);
    int getType(); // 根据类型不同选择不同的序列化方式

    // 定义静态常量
    static final Map<Integer, Serializer> serializerMap = new HashMap<>();

    // problem: why getSerializerByCode is a static method?
    // 使用map存储序列化器
    static Serializer getSerializerByCode(int code) {
        // 初始化map
        if (serializerMap.isEmpty()) {
            serializerMap.put(0, new ObjectSerializer());
            serializerMap.put(1, new JsonSerializer());
            serializerMap.put(2, new KryoSerializer());
            serializerMap.put(3, new HessianSerializer());
            serializerMap.put(4, new ProtostuffSerializer());
        }
        return serializerMap.get(code); // 不存在则返回null
    }
}