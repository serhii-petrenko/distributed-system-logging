package com.course.bff.authors.handlers;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ControllerExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);
    private MeterRegistry meterRegistry;

    @Autowired
    public ControllerExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleInternalError(Exception e) {
        countHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        logger.error("Returned HTTP Status 500 due to the following exception:", e);
        return "Internal Server Error";
    }

    private void countHttpStatus(HttpStatus status) {
        meterRegistry.counter(String.format("error_count", status.value()), "AuthorController", "authors-service").increment();
    }

}
