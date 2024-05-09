package com.endava.demo.connector.model.internal;

import lombok.Getter;

@Getter
public enum ErrorType {
    BF_ERROR(9300, "Booking failed."),
    BF_CONNECTION_TIMEOUT(9301, "Booking failed because of a timeout on thirdparty"),
    BF_LONG_WISHES(9302, "Booking failed because of too long wishes."),
    BF_SOLD_OUT(9303, "Booking failed because rate is sold out"),
    BF_NOT_DONE(9304, "Booking failed because third party could not process booking");

    ErrorType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;

    @Override
    public String toString() {
        return "ErrorType{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
