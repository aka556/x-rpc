package org.xiaoyu.core.test.serializer;

import org.junit.Test;
import org.xiaoyu.api.pojo.User;
import org.xiaoyu.common.exception.SerializeException;
import org.xiaoyu.common.serializer.gumiSerializer.ProtostuffSerializer;

import static org.junit.Assert.*;

public class ProtostuffSerializerTest {
    private ProtostuffSerializer protostuffSerializer = new ProtostuffSerializer();

    @Test
    public void testSerializeAndDeserialize() {
        User user = User.builder().id(1).userName("xiaoyu").gender(true).build();

        // 序列化
        byte[] bytes = protostuffSerializer.serialize(user);
        assertNotNull("serialize result should not be null", bytes);

        // 反序列化
        Object deserializedUser = protostuffSerializer.deserialize(bytes, 1);
        assertNotNull("deserialize result should not be null", deserializedUser);

        // 对象属性相等
        assertEquals("deserialized object should be equal to original object", user, deserializedUser);
    }

    @Test
    public void testSerializeNullObject() {
        // 序列化null对象
        try {
            protostuffSerializer.serialize(null);
            fail("Expected NullPointerException for null object");
        } catch(IllegalArgumentException e) {
            assertEquals("serialize object is null", e.getMessage());
        }
    }

    @Test
    public void testDeserializeNullOBytes() {
        try {
            protostuffSerializer.deserialize(null, 1);
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot deserialize null or empty byte array", e.getMessage());
        }
    }

    @Test
    public void testDeserializeEmptyBytes() {
        try {
            protostuffSerializer.deserialize(null, 1);
            fail("Expected SerializeException for null bytes");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot deserialize null or empty byte array", e.getMessage());
        }
    }

    @Test
    public void testDeserializeWithUnknownMessageType() {
        byte[] bytes = new byte[]{1, 2, 3};
        try {
            protostuffSerializer.deserialize(bytes, 5);
            fail("Expected SerializeException for unknown message type");
        } catch (SerializeException e) {
            assertEquals("Unknown message type: 5", e.getMessage());
        }
    }
}
