package com.s8.demoservice.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {
    private int code;
    private String message;
}
