package org.xiaoyu.core.trace.Interceptor;

import org.xiaoyu.common.trace.TraceContext;
import org.xiaoyu.core.trace.TraceIdGenerator;
import org.xiaoyu.core.trace.ZipkinReporter;

/**
 * 链路追踪拦截器
 */
public class ClientTraceInterceptor {
    public static void beforeInvoke() {
        String traceId = TraceContext.getTraceId();
        if (traceId == null) {
            traceId = TraceIdGenerator.generateTraceId();
            TraceContext.setTraceId(traceId);
        }
        String spanId = TraceIdGenerator.generateSpanId();
        TraceContext.setSpanId(spanId);

        // 记录客户端span
        long startTimeStamp = System.currentTimeMillis();
        TraceContext.setStartTimeStamp(String.valueOf(startTimeStamp));
    }

    public static void afterInvoke(String serviceName) {
        long startTimeStamp = Long.parseLong(TraceContext.getStartTimeStamp());
        long endTimeStamp = System.currentTimeMillis();
        long duration = endTimeStamp - startTimeStamp;

        // 上报span信息
        ZipkinReporter.reportSpan(
                TraceContext.getTraceId(),
                TraceContext.getSpanId(),
                TraceContext.getParentSpanId(),
                "Client-" + serviceName,
                startTimeStamp,
                duration,
                serviceName,
                "Client");

        TraceContext.clear();
    }
}
