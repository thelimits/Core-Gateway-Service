package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionRequestDTO {
    private String accountNumber;
    private String accountNumberCreditor;
    private String description;
    private String amount;
    private String denomination = "IDR";
}
