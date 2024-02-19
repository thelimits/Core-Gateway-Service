package com.s8.demoservice.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateCustomerDTO {
    private String phoneNumber;
    private String pin;
}
