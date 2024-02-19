package com.s8.demoservice.exception;

public class BadRequest extends RuntimeException{
    public BadRequest(String message) {
        super(message);
    }
}
