package org.xiaoyu.common.serializer.gumiSerializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import org.xiaoyu.common.exception.SerializeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian序列化方法
 */
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        // 使用ByteArrayOutputStream 和 HessianOutput来序列化对象
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj); // 将对象写入输出流
            return byteArrayOutputStream.toByteArray(); // 返回字节数组
        } catch (IOException e) {
            throw new SerializeException("Serialization failed");
        }
    }

    // 反序列化操作
    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        // 使用ByteArrayInputStream 和 HessianInput来反序列化对象
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            return hessianInput.readObject(); // 读取对象
        } catch (IOException e) {
            throw new SerializeException("Deserialization failed");
        }
    }

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public String toString() {
        return "HessianSerializer";
    }
}
