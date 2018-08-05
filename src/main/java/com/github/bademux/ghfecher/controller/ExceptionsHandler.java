package com.github.bademux.ghfecher.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@ControllerAdvice
@Slf4j
public class ExceptionsHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<?> handleNapsClientError(HttpClientErrorException e) {
        log.error("Client error", e);
        return ResponseEntity.status(e.getRawStatusCode()).body(getBodyForClientError(e));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<?> handleNapsServerError(HttpServerErrorException e) {
        log.error("Server error", e);
        return createResponseEntity(e.getStatusCode());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleGenericrError(Throwable e) {
        log.error("Unknown error", e);
        return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<String> createResponseEntity(HttpStatus status) {
        return ResponseEntity.status(status.value()).body(status.getReasonPhrase());
    }

    private String getBodyForClientError(HttpClientErrorException e) {
        return StringUtils.isEmpty(e.getMessage()) ? e.getStatusText() : e.getMessage();
    }

}
