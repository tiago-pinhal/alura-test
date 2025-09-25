package br.com.alura.AluraFake.config;

import br.com.alura.AluraFake.exception.ErrorResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@RestControllerAdvice
public class TaskControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError(IllegalArgumentException ex) {
        return new ErrorResponse(ex.getMessage());
    }
}