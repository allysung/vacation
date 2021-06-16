package com.storm.vacation.exception;

import com.storm.vacation.common.ErrorsResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Web Exception handler
 */
@ControllerAdvice
@Slf4j
public class VacationExceptionHandler {

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity exceptionHandle(IllegalArgumentException e) {
        log.error("exceptionHandle", e);
        var content = ErrorsResource.ErrorContent.builder()
                .message(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(ErrorsResource.buildErrorsResource(content));
    }

}
