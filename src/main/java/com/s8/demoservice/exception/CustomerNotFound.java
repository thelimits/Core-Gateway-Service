package com.s8.demoservice.exception;

public class CustomerNotFound extends RuntimeException{
    public CustomerNotFound(String message) {
        super(message);
    }
}
