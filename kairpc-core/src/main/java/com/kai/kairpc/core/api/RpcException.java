package com.kai.kairpc.core.api;

import lombok.Data;

/**
 * RPC 统一异常类
 */
@Data
public class RpcException extends RuntimeException {

    private String errCode;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RpcException(Throwable cause, String errCode) {
        super(cause);
        this.errCode = errCode;
    }

    public RpcException(String message, String errCode) {
        super(message);
        this.errCode = errCode;
    }

    // X: 技术类异常
    // Y: 业务类异常
    // Z: unknown, 暂时搞不清楚，搞清楚之后再归类到 X 或者 Y
    public static final String SOCKET_TIMEOUT_EX = "X001" + "-" + "http_invoke_timeout";
    public static final String NO_SUCH_METHOD_EX = "X002" + "-" + "method_not_exists";

    public static final String EXCEED_LIMIT_EX = "X003" + "-" + "tps_exceed_limit";
    public static final String UNKNOWN = "Z001" + "-" + "unknown";
}
