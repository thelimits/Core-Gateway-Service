package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountTransactionDTO {
    private List<TransactionDTO> posting_instruction_batches;
    private String previous_page_token;
    private String next_page_token;

    public AccountTransactionDTO(List<TransactionDTO> transactionDTOS) {
        this.posting_instruction_batches = transactionDTOS;
    }
}
