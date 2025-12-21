package org.xiaoyu.core.client.netty.nettyInitializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.xiaoyu.common.serializer.gumiCode.Decoder;
import org.xiaoyu.common.serializer.gumiCode.Encoder;
import org.xiaoyu.common.serializer.gumiSerializer.JsonSerializer;
import org.xiaoyu.core.client.netty.handler.NettyClientHandler;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //初始化，每个 SocketChannel 都有一个独立的管道（Pipeline），用于定义数据的处理流程
        ChannelPipeline pipeline = ch.pipeline();
        // 使用自定义解码器与编码器
        pipeline.addLast(new Encoder(new JsonSerializer()));
        pipeline.addLast(new Decoder());
        pipeline.addLast(new NettyClientHandler());
    }
}
