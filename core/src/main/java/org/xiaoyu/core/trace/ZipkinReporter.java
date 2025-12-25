package org.xiaoyu.core.trace;

import lombok.extern.slf4j.Slf4j;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * ZipkinReporter
 * 封装zipkin的调用
 */
@Slf4j
public class ZipkinReporter {
    private static final String ZIPKIN_URL = "http://localhost:9411/api/v2/spans";
    private static AsyncReporter<Span> reporter;

    static {
        // 初始化zipkin上报器
        OkHttpSender sender = OkHttpSender.create(ZIPKIN_URL);
        reporter = AsyncReporter.create(sender);
    }

    /**
     * 上报span到zipkin
     */
    public static void reportSpan(String traceId, String spanId, String parentSpanId,
                                  String name, long startTimeStamp, long duration,
                                  String serviceName, String type) {
        Span span = Span.newBuilder()
                .traceId(traceId)
                .id(spanId)
                .parentId(parentSpanId)
                .name(name)
                .timestamp(startTimeStamp * 1000) // 使用微秒
                .duration(duration * 1000)
                .putTag("service", serviceName)
                .putTag("type", type)
                .build();

        reporter.report(span);
        log.info("当前traceId{}——上报信息", traceId);
    }

    // 关闭
    public static void close() {
        reporter.close();
    }
}
