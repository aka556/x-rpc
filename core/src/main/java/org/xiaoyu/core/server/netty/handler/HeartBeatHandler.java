package org.xiaoyu.core.server.netty.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 处理IdleStateEvent的读空闲事件READ_IDLE
        try {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                IdleState idleState = ((IdleStateEvent) evt).state();

                // 如果读空闲，则发送心跳包
                if (idleState == IdleState.READER_IDLE) {
                    log.info("超过10秒没有收到客户端心跳, channel:{}", ctx.channel());
                    ctx.close();
                } else if (idleState == IdleState.WRITER_IDLE) {
                    log.info("超过20秒没有写数据, channel:{}", ctx.channel());
                    ctx.close();
                }
            }
        } catch (Exception e) {
            log.error("服务端处理心跳异常", e);
        }
    }
}
