package com.sample.trade.validationstorage.exception;

public class TradeProjectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TradeProjectionException(String message) {
        super(message);
    }

    public TradeProjectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TradeProjectionException(Throwable cause) {
        super(cause);
    }
}
