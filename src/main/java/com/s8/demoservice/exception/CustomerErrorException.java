package com.s8.demoservice.exception;

public class CustomerErrorException extends RuntimeException{
    public CustomerErrorException(String message) {
        super(message);
    }
}
