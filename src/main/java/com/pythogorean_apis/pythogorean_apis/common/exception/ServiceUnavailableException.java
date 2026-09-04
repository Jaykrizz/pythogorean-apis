package com.pythogorean_apis.pythogorean_apis.common.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends ApiException {

    public ServiceUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
