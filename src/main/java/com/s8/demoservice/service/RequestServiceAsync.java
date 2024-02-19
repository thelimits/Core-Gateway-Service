package com.s8.demoservice.service;

import com.s8.demoservice.AppConfiguration;
import com.s8.demoservice.exception.BadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class RequestServiceAsync {
    @Autowired
    private AppConfiguration configure;

    public <T> CompletableFuture<ResponseEntity<?>> processRequest(Supplier<T> supplier) {
        CompletableFuture<?> future = CompletableFuture.supplyAsync(supplier);

        return future.completeOnTimeout(null, configure.getTIMEOUT_CLIENT(), TimeUnit.MILLISECONDS)
                .thenComposeAsync(result -> {
                    if (result != null) {
                        return CompletableFuture.completedFuture(ResponseEntity.ok(result));
                    } else {
                        Map<String, Object> errorResponse = createErrorResponse(HttpStatus.REQUEST_TIMEOUT.value(), "Timeout occurred.");
                        return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse));
                    }
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof BadRequest badRequest) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(HttpStatus.BAD_REQUEST.value(), badRequest.getMessage()));
                    }else if (ex.getCause() instanceof ServiceUnavailable ServiceUnavailable){
                        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), ServiceUnavailable.getMessage()));
                    }else if (ex.getCause() instanceof NotFound NotFound){
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(HttpStatus.NOT_FOUND.value(), NotFound.getMessage()));
                    }else if (ex.getCause() instanceof Unauthorized Unauthorized){
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse(HttpStatus.UNAUTHORIZED.value(), Unauthorized.getMessage()));
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
                });
    }

    private Map<String, Object> createErrorResponse(int code, String error) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", code);
        errorResponse.put("error", error);
        return errorResponse;
    }
}
