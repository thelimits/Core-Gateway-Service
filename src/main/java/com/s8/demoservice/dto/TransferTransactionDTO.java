package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferTransactionDTO {
    private Double amount;
    private String denomination;
    private String debtor_target_account_id;
    private String creditor_target_account_id;
}
