package com.alphatica.genotick.data;

public class NoDataForTimePointException extends RuntimeException {
    public NoDataForTimePointException(String message) {
        super(message);
    }
}
