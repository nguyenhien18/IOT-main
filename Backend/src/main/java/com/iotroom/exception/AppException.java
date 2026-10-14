package com.iotroom.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.iotroom.enums.ErrorCode;

public class AppException extends RuntimeException {
    private final int code;
    private final HttpStatusCode status;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.status = errorCode.getStatusCode();
    }

    public AppException(HttpStatus status, String message) {
        super(message);
        this.code = status.value();
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}

