package org.xiaoyu.core.server.netty.nettyInitializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;
import org.xiaoyu.common.serializer.gumiCode.Decoder;
import org.xiaoyu.common.serializer.gumiCode.Encoder;
import org.xiaoyu.common.serializer.gumiSerializer.JsonSerializer;
import org.xiaoyu.core.server.netty.handler.NettyServerHandler;
import org.xiaoyu.core.server.provider.ServiceProvider;

@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;

    @Override
    protected void initChannel(SocketChannel sh) throws Exception {
        ChannelPipeline pipeline = sh.pipeline();
        // 使用自定义的解码器与编码器
        pipeline.addLast(new Encoder(new JsonSerializer()));
        pipeline.addLast(new Decoder());
        pipeline.addLast(new NettyServerHandler(serviceProvider));
    }
}
