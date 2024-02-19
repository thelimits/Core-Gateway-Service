package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Map;


@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionTotalExpensesDTO {
    private Map<String, TransactionExpensesDTO> transactionExpensesDTOMap;
    private Double totalAmountExpenses = 0.00;
    private String description;

    public TransactionTotalExpensesDTO(Map<String, TransactionExpensesDTO> transactionExpensesDTOMap, String description){
        this.transactionExpensesDTOMap = transactionExpensesDTOMap;
        this.description = description;

        totalAmountExpenses = transactionExpensesDTOMap.values().stream()
                .mapToDouble(TransactionExpensesDTO::getAmountExpenses)
                .sum();

    }
}
