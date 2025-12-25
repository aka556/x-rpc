package org.xiaoyu.core.client.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.common.trace.TraceContext;

import java.util.Map;

/**
 * MDCChannelHandler 异常处理
 */
@Slf4j
public class MDCChannelHandler extends ChannelOutboundHandlerAdapter {
    public static final AttributeKey<Map<String, String>> TRACE_CONTEXT_KEY = AttributeKey.valueOf("TraceContext");

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) throws Exception {
        // 从Channel 上下文中获取 TraceContext
        Map<String, String> traceContext = ctx.channel().attr(TRACE_CONTEXT_KEY).get();
        if (traceContext != null) {
            // 设置当前线程的 TraceContext
            TraceContext.clone(traceContext);
            log.info("已绑定trace上下文: {}", traceContext);
        } else {
            log.error("未找到上下文信息");
        }
        super.write(ctx, msg, promise);
    }
}
