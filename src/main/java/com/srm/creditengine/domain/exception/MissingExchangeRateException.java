package com.srm.creditengine.domain.exception;

public class MissingExchangeRateException extends RuntimeException {
    public MissingExchangeRateException(String message) {
        super(message);
    }
}
