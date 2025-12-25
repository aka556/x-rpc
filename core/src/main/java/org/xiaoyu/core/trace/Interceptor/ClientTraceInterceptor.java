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
            traceId = TraceIdGenerator.TraceGenerator();
            TraceContext.setTraceId(traceId);
        }
        String spanId = TraceContext.getSpanId();
        TraceContext.setSpanId(spanId);

        // 记录客户端span
        long startTimeStamp = System.currentTimeMillis();
        TraceContext.setStartTimeStamp(String.valueOf(startTimeStamp));
    }

    public static void afterInvoke(String serviceName) {
        long startTimeStamp = System.currentTimeMillis();
        long endTimeStamp = Long.parseLong(TraceContext.getStartTimeStamp());
        long duration = endTimeStamp - startTimeStamp;

        // 上报span信息
        ZipkinReporter.reportSpan(
                TraceContext.getTraceId(),
                TraceContext.getSpanId(),
                TraceContext.getParentSpanId(),
                "Client--" + serviceName,
                startTimeStamp,
                duration,
                serviceName,
                "Client");

        TraceContext.clear();
    }
}
