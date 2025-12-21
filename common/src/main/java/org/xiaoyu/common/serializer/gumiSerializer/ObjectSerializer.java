package org.xiaoyu.common.serializer.gumiSerializer;

import java.io.*;

/**
 * Java自带的序列化器
 */
public class ObjectSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes = null;
        // 创建一个内存中的输出流，用于存储序列化后的字节数据
        // ByteArrayOutputStream是一个可变大小的字节数据缓冲区，数据都会写入这个缓冲区中
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            // 把对象写入输出流中，触发序列化
            oos.writeObject(obj);
            oos.flush();
            // 将字节缓冲区的内容转换为字节数组
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        // 将字节数组包装成一个字节输入流
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            // 使用ObjectInputStream包装一个ByteArrayInputStream对象
            ObjectInputStream ois = new ObjectInputStream(bis);
            // 从ois中读取序列化对象那个，并反序列化
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String toString() {
        return "ObjectSerializer";
    }
}
