package com.s8.demoservice.exception;

public class CustomerAuthError extends RuntimeException{
    public CustomerAuthError(String message) {
        super(message);
    }
}
