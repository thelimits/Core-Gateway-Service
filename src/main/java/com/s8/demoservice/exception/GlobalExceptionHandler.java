package com.s8.demoservice.exception;

import com.s8.demoservice.util.StringUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomerErrorException.class)
    public ResponseEntity<Object> handleCustomerErrorException(CustomerErrorException ex) {
        ErrorResponse body = new ErrorResponse(INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(CustomerAuthError.class)
    public ResponseEntity<Object> handleCustomerAuthError(CustomerAuthError ex) {
        ErrorResponse body = new ErrorResponse(UNAUTHORIZED.value(), ex.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(CustomerNotFound.class)
    public ResponseEntity<Object> handleCustomerNotFound(CustomerNotFound ex) {
        ErrorResponse body = new ErrorResponse(NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(body);
    }

    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<Object> handleParameterBadRequest(CustomerNotFound ex) {
        ErrorResponse body = new ErrorResponse(BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnknownException(Exception ex) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(ex.toString());
    }

    @ExceptionHandler({
            HttpServerErrorException.class
            , HttpClientErrorException.class
    })
    public ResponseEntity<Object> handle(HttpStatusCodeException ex,
                                         HttpServletRequest request, HttpServletResponse response) throws Exception{
        return new ResponseEntity(StringUtil.convertJsonStringToMap(ex.getResponseBodyAsString())
                , ex.getStatusCode());
    }
}
