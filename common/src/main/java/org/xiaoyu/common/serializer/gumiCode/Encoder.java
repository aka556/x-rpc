package org.xiaoyu.common.serializer.gumiCode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaoyu.common.message.MessageType;
import org.xiaoyu.common.message.RpcRequest;
import org.xiaoyu.common.message.RpcResponse;
import org.xiaoyu.common.serializer.gumiSerializer.Serializer;
import org.xiaoyu.common.trace.TraceContext;

/**
 * MessageToByteEncoder是netty专门设计用来实现编码器得抽象类，可以帮助开发者将Java对象编码成字节数据
 */
@AllArgsConstructor
@Slf4j
public class Encoder extends MessageToByteEncoder {
    private Serializer serializer; // 序列化对象

    // netty在写出数据时会调用这个方法，将Java对象编码成二进制数据
    // 参数ctx 是netty提供得上下文对象，代表管道上下文，包含通道和处理器相关信息。
    // 参数msg是要编码得消息对象
    // 参数out 是netty提供的字节缓冲区，编码后的字节数据写入其中
    @Override
    protected void encode(ChannelHandlerContext ctx, Object message, ByteBuf byteBuf) throws Exception {
        log.debug("Encode message of type: {}", message.getClass());

        // 写入trace消息头
        String traceMsg = TraceContext.getTraceId() + ";" + TraceContext.getSpanId();
        byte[] traceMsgBytes = traceMsg.getBytes();
        byteBuf.writeInt(traceMsgBytes.length);
        byteBuf.writeBytes(traceMsgBytes);

        // 判断消息类型
        if (message instanceof RpcRequest) {
            byteBuf.writeShort(MessageType.REQUEST.getCode());
        } else if (message instanceof RpcResponse) {
            byteBuf.writeShort(MessageType.RESPONSE.getCode());
        }
        // 写入当前序列化器的标识类型
        byteBuf.writeShort(serializer.getType());
        // 将消息转换为字节数组
        byte[] serializeBytes = serializer.serialize(message);
        if (serializeBytes == null || serializeBytes.length == 0) {
            throw new IllegalArgumentException("Serialized message is null or empty");
        }

        // 写入消息的字节长度
        byteBuf.writeInt(serializeBytes.length);
        byteBuf.writeBytes(serializeBytes);
    }

}
