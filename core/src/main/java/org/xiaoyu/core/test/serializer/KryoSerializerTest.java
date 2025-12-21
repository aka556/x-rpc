package org.xiaoyu.core.test.serializer;

import org.junit.Test;
import org.xiaoyu.api.pojo.User;
import org.xiaoyu.common.exception.SerializeException;
import org.xiaoyu.common.serializer.gumiSerializer.KryoSerializer;

import static org.junit.Assert.*;
public class KryoSerializerTest {
    private KryoSerializer kryoSerializer = new KryoSerializer(); // 创建序列化器

    @Test
    public void testSerializeAndDeserialize() {
        // 创建一个User对象
        User user = User.builder().id(1).userName("test1").gender(true).build();

        // 序列化
        byte[] bytes = kryoSerializer.serialize(user);
        assertNotNull("序列化结果不为null", bytes);

        // 反序列化
        Object deserializedUser = kryoSerializer.deserialize(bytes, 1);
        assertNotNull("反序列化结果不为null", deserializedUser);

        // 验证反序列化后的对象与原始对象是否相等
        assertTrue("反序列化后的对象了类型应该是User", deserializedUser instanceof User);
        User newUser = (User) deserializedUser;
        assertEquals("反序列化后的对象属性与原始对象相等", user, newUser);
    }

    @Test
    public void testSerializeNullObject() {
        try {
            kryoSerializer.serialize(null);
            fail("Expected SerializeException for null input");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot serialize null object", e.getMessage());
        }
    }

    @Test
    public void testDeserializerNullObject() {
        try {
            kryoSerializer.deserialize(null, 1);
            fail("Expected SerializeException for null input");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot deserialize null or empty byte array", e.getMessage());
        }
    }


    @Test
    public void testDeserializeEmptyBytes() {
        byte[] bytes = new byte[0];
        try {
            kryoSerializer.deserialize(bytes, 1);
            fail("Expected SerializeException for empty bytes");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot deserialize null or empty byte array", e.getMessage());
        }
    }

    @Test
    public void testDeserializerInvalidMessageType() {
        byte[] bytes = kryoSerializer.serialize(new User(1, "MyTest",  true));
        try {
            kryoSerializer.deserialize(bytes, 0);
            fail("Expected SerializeException for invalid message type");
        } catch (SerializeException e) {
            assertEquals("Deserialization failed", e.getMessage());
        }
    }
}
