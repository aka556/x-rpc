package org.xiaoyu.common.serializer.gumiCode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.common.message.MessageType;
import org.xiaoyu.common.serializer.gumiSerializer.Serializer;
import org.xiaoyu.common.trace.TraceContext;

import java.util.List;
@Slf4j
@AllArgsConstructor
public class Decoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 检查可读字节数
        if (in.readableBytes() < 8) {
            return; // messageType + serializerType + length
        }

        // 读取trace message
        int traceLength = in.readInt();
        byte[] traceBytes = new byte[traceLength];
        in.readBytes(traceBytes);
        serializeTraceMsg(traceBytes);

        // 读取消息类型
        short messageType = in.readShort();
        // 判断类型
        if (messageType != MessageType.REQUEST.getCode() && messageType != MessageType.RESPONSE.getCode()) {
            log.info("暂不支持该消息类型");
            return;
        }

        // 读取序列化类型
        short serializerType = in.readShort();
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if (serializer == null) {
            throw new RuntimeException("不存在对应的序列化器");
        }

        // 读取消息长度和数据
        int length = in.readInt();
        // 数据长度不足, return等待更多数据
        if (in.readableBytes() < length) {
            return;
        }

        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        // 反序列化对象后赋值变量
        // 罪魁祸首，传递了serializerType, 应该是messageType
        // messageType用于指定消息类型，而serializerType用于指定序列化器
        Object deserialize = serializer.deserialize(bytes, messageType);
        out.add(deserialize);
    }

    //解析并存储traceMsg
    private void serializeTraceMsg(byte[] traceByte){
        String traceMsg = new String(traceByte);
        String[] msg = traceMsg.split(";");
        if(!msg[0].isEmpty()) TraceContext.setTraceId(msg[0]);
        if(!msg[1].isEmpty()) TraceContext.setParentSpanId(msg[1]);
    }
}
