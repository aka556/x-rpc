package org.xiaoyu.core.trace.Interceptor;

import org.xiaoyu.common.trace.TraceContext;
import org.xiaoyu.core.trace.TraceIdGenerator;
import org.xiaoyu.core.trace.ZipkinReporter;

/**
 * 服务端链路追踪拦截器
 */
public class ServerTraceInterceptor {
    public static void beforeHandle() {
        String traceId = TraceContext.getTraceId();
        String parentSpanId = TraceContext.getParentSpanId();
        String spanId = TraceIdGenerator.generateSpanId();
        TraceContext.setTraceId(traceId);
        TraceContext.setSpanId(spanId);
        TraceContext.setParentSpanId(parentSpanId);

        long startTimeStamp = System.currentTimeMillis();
        TraceContext.setStartTimeStamp(String.valueOf(startTimeStamp));
    }

    public static void afterHandle(String serviceName) {
        long startTimeStamp = Long.parseLong(TraceContext.getStartTimeStamp());
        long endTimeStamp = System.currentTimeMillis();
        long duration = endTimeStamp - startTimeStamp;

        ZipkinReporter.reportSpan(
                TraceContext.getTraceId(),
                TraceContext.getSpanId(),
                TraceContext.getParentSpanId(),
                "server-" + serviceName,
                startTimeStamp,
                duration,
                serviceName,
                "server");

        // 清理TraceContext
        TraceContext.clear();
    }
}
