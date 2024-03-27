package com.kai.kairpc.core.api;

public class KaiRpcException extends RuntimeException {

    private String errCode;

    public KaiRpcException() {
    }

    public KaiRpcException(String message) {
        super(message);
    }

    public KaiRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public KaiRpcException(Throwable cause) {
        super(cause);
    }

    public KaiRpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public KaiRpcException(Throwable cause, String errCode) {
        super(cause);
        this.errCode = errCode;
    }

    // X: 技术类异常
    // Y: 业务类异常
    // Z: unknown, 暂时搞不清楚，搞清楚之后再归类到 X 或者 Y
    public static final String SOCKET_TIMEOUT_EX = "X001" + "-" + "http_invoke_timeout";
    public static final String NO_SUCH_METHOD_EX = "X002" + "-" + "method_not_exists";
    public static final String UNKNOWN = "Z001" + "-" + "unknown";
}
