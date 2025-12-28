package org.xiaoyu.core.client.netty.nettyInitializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.common.serializer.gumiCode.Decoder;
import org.xiaoyu.common.serializer.gumiCode.Encoder;
import org.xiaoyu.common.serializer.gumiSerializer.Serializer;
import org.xiaoyu.core.client.netty.handler.HeartBeatHandler;
import org.xiaoyu.core.client.netty.handler.MDCChannelHandler;
import org.xiaoyu.core.client.netty.handler.NettyClientHandler;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    public NettyClientInitializer() {}

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //初始化，每个 SocketChannel 都有一个独立的管道（Pipeline），用于定义数据的处理流程
        ChannelPipeline pipeline = ch.pipeline();
        try {
            // 使用自定义解码器与编码器
            pipeline.addLast(new Encoder(Serializer.getSerializerByCode(3)));
            pipeline.addLast(new Decoder());
            pipeline.addLast(new NettyClientHandler());
            // 添加 MDCChannelHandler, 用于记录请求ID
            pipeline.addLast(new MDCChannelHandler());
            pipeline.addLast(new IdleStateHandler(0, 8, 0, TimeUnit.SECONDS));
            // 添加心跳检测
            pipeline.addLast(new HeartBeatHandler());
            log.info("Netty client pipeline initialized with serializer type: {}",Serializer.getSerializerByCode(3).getType());
        } catch (Exception e) {
            log.error("Netty client initialization failed", e);
            throw e; // 抛出异常
        }
    }
}
