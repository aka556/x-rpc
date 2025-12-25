package org.xiaoyu.common.trace;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

/**
 * TraceContext
 * 保存链路信息 traceId, spanId
 */
@Slf4j
public class TraceContext {
    public static void setTraceId(String traceId) {
        MDC.put("traceId", traceId);
    }

    public static String getTraceId() {
        return MDC.get("traceId");
    }

    public static void setSpanId(String spanId) {
        MDC.put("spanId", spanId);
    }

    public static String getSpanId() {
        return MDC.get("spanId");
    }

    public static void setParentSpanId(String parentSpanId) {
        MDC.put("parentSpanId", parentSpanId);
    }

    public static String getParentSpanId() {
        return MDC.get("parentSpanId");
    }

    public static void setStartTimeStamp(String startTimeStamp) {
        MDC.put("startTimeStamp", startTimeStamp);
    }

    public static String getStartTimeStamp() {
        return MDC.get("startTimeStamp");
    }

    // 获取MDC的副本，用于传递给子线程
    public static Map<String, String> getCopy() {
        return MDC.getCopyOfContextMap();
    }

    public static void clone(Map<String, String> context) {
        for (Map.Entry<String, String> entry : context.entrySet()) {
            MDC.put(entry.getKey(), entry.getValue());
        }
    }

    public static void clear() {
        MDC.clear();
    }
}
