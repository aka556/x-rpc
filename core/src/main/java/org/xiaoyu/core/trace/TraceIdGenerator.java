package org.xiaoyu.core.trace;

/**
 * TraceId生成器
 * 采用雪花算法生成全局唯一ID
 */
public class TraceIdGenerator {
    // 单机器id使用0，在具体环境中可以在配置文件中配置
    private static final long WORKER_ID = 0;
    private static final SnowflakeGenerator SNOWFLAKE = new SnowflakeGenerator(WORKER_ID);

    public static String TraceGenerator() {
        return Long.toHexString(SNOWFLAKE.nextId());
    }

    static class SnowflakeGenerator {
        // 机器id
        private final long workerId;
        // 基准时间
        private final long epoch = 1609459200000L;
        // 序列号
        private long sequence = 0L;
        // 上一次生成时间的时间戳
        private long lastTimestamp = -1L;

        public SnowflakeGenerator(long workerId) {
            // 验证workerId是否在0-1023之间
            if (workerId < 0 || workerId > 1023) {
                throw new IllegalArgumentException("workerId must be between 0 and 1023");
            }
            this.workerId = workerId;
        }

        // 获取下一个ID
        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();
            if (timestamp < lastTimestamp) {
                throw new IllegalStateException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
            }

            // 如果当前时间戳与上一次生成ID的时间戳相同，则使用序列号自增
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & 0xFFF;
                if (sequence == 0) {
                    // 如果序列号自增到最大值，则等待下一个毫秒再生成ID
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            // 更新上一次生成ID的时间戳
            lastTimestamp = timestamp;
            // 生成ID
            return ((timestamp - epoch) << 22) | (workerId << 12) | sequence;
        }

        // 获取下一个时间戳，直到获取到比当前时间戳大的时间戳
        private long tilNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }
}
