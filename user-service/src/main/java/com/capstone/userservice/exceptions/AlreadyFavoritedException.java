package com.capstone.userservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class AlreadyFavoritedException extends RuntimeException {
    public AlreadyFavoritedException(String message) {
        super(message);
    }
}
