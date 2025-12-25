package org.xiaoyu.core.server.netty.nettyInitializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;
import org.xiaoyu.common.serializer.gumiCode.Decoder;
import org.xiaoyu.common.serializer.gumiCode.Encoder;
import org.xiaoyu.common.serializer.gumiSerializer.Serializer;
import org.xiaoyu.core.client.netty.handler.HeartBeatHandler;
import org.xiaoyu.core.server.netty.handler.NettyServerHandler;
import org.xiaoyu.core.server.provider.ServiceProvider;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;

    @Override
    protected void initChannel(SocketChannel sh) throws Exception {
        ChannelPipeline pipeline = sh.pipeline();
        // 服务端关注读事件与写事件，10秒内没有没有收到客户端的消息，将触发IdleState.WRITER_IDLE
        pipeline.addLast(new IdleStateHandler(10, 20, 0, TimeUnit.SECONDS));
        pipeline.addLast(new HeartBeatHandler());

        // 使用自定义的解码器与编码器
        pipeline.addLast(new Encoder(Serializer.getSerializerByCode(3)));
        pipeline.addLast(new Decoder());
        pipeline.addLast(new NettyServerHandler(serviceProvider));
    }
}
