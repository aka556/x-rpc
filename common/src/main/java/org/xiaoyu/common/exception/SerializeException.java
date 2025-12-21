package org.xiaoyu.common.exception;

/**
 * 异常处理类
 */
public class SerializeException extends RuntimeException {
    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(String message, Throwable cause) {
        super(message, cause);
    }
}
