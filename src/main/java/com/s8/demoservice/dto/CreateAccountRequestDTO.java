package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.s8.demoservice.model.AccountAdditionalDetails;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAccountRequestDTO {
    private String productVersionId;
    private String baseCurrency;
    private String accountName;
    private InstanceParamDTO instanceParameterDTO;
    private InstanceParamLoanDTO instanceParameterLoan;
    private InstanceParamBnplDTO instanceParameterBnpl;
    private AccountAdditionalDetails accountAdditionalDetails;
}
