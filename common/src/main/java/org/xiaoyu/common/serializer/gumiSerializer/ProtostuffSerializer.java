package org.xiaoyu.common.serializer.gumiSerializer;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.xiaoyu.api.pojo.User;
import org.xiaoyu.common.exception.SerializeException;

/**
 * ProtostuffSerializer序列化方法
 */
public class ProtostuffSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        // 检查null对象
        if (obj == null) {
            throw new IllegalArgumentException("serialize object is null");
        }
        // 获取对象的schema
        Schema schema = RuntimeSchema.getSchema(obj.getClass());

        // 使用LinkedBuffer创建一个LinkedBuffer对象，用于存储序列化后的数据
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        // 序列化对象为字节数组
        byte[] bytes;
        try {
            bytes = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Cannot deserialize null or empty byte array");
        }

        // 根据 messageType 来决定反序列化的类，这里假设 `messageType` 是类的标识符
        Class<?> clazz = getClassForMessageType(messageType);

        // 获取对象的schema
        Schema schema = RuntimeSchema.getSchema(clazz);

        // 床架一个schema
        Object object;
        try {
            object = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new SerializeException("Deserialization failed due to reflection issues");
        }

        // 反序列化字节数组为对象
        ProtostuffIOUtil.mergeFrom(bytes, object, schema);
        return object;
    }

    @Override
    public int getType() {
        return 4;
    }

    // 用于根据messageType获取对应的类
    private Class<?> getClassForMessageType(int messageType) {
        if (messageType == 1) {
            return User.class;
        } else {
            throw new SerializeException("Unknown message type: " + messageType);
        }
    }

    @Override
    public String toString() {
        return "ProtostuffSerializer";
    }
}
