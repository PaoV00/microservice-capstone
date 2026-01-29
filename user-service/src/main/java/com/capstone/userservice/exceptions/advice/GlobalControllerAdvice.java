package com.capstone.userservice.exceptions.advice;

import com.capstone.userservice.exceptions.BadInputException;
import com.capstone.userservice.exceptions.DuplicateUserException;
import com.capstone.userservice.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(UserNotFoundException ex) {
        return Map.of(
                "error", ex.getMessage(),
                "status", 404
        );
    }

    @ExceptionHandler(BadInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBadInput(BadInputException ex) {
        return Map.of(
                "error", ex.getMessage(),
                "status", 400
        );
    }

    @ExceptionHandler(DuplicateUserException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleDuplicate(DuplicateUserException ex) {
        return Map.of(
                "error", ex.getMessage(),
                "status", 409
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

        return Map.of(
                "error", "Validation failed",
                "details", errors,
                "status", 400
        );
    }
}
