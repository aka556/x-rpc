package org.xiaoyu.core.test.serializer;

import org.junit.Test;
import org.xiaoyu.api.pojo.User;
import org.xiaoyu.common.exception.SerializeException;
import org.xiaoyu.common.serializer.gumiSerializer.HessianSerializer;

import static org.junit.Assert.*;

public class HessianSerializerTest {
    private HessianSerializer hessianSerializer = new HessianSerializer();

    @Test
    public void testSerializeAndDeserialize() {
        // 创建测试对象
        User user = User.builder().id(1).userName("xiaoyu").gender(true).build();

        // 序列化对象
        byte[] bytes = hessianSerializer.serialize(user);
        assertNotNull("序列化结果不为null", bytes);

        // 反序列对象
        Object deserializedUser = hessianSerializer.deserialize(bytes, 1);
        assertNotNull("反序列化结果不为null", deserializedUser);

        // 对象属性相等
        assertEquals("反序列化后的对象属性与原始对象相等", user, deserializedUser);
    }

    @Test
    public void testDeserializeWithInvalidData() {
        byte[] invalidData = new byte[]{1, 2, 3};
        try {
            hessianSerializer.deserialize(invalidData, 3);
            fail("Expected IllegalArgumentException for invalid data");
        } catch (SerializeException e) {
            assertEquals("Deserialization failed", e.getMessage());
        }
    }
}
