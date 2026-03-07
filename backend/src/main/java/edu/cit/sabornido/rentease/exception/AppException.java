package edu.cit.sabornido.rentease.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private final String code;
    private final Object details;
    private final HttpStatus status;

    public AppException(String code, String message, Object details, HttpStatus status) {
        super(message);
        this.code = code;
        this.details = details;
        this.status = status;
    }
}
