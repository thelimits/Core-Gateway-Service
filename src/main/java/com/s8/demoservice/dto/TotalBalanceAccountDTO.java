package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.s8.demoservice.model.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Setter
@Getter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TotalBalanceAccountDTO {
    private List<Account> accountList;
    private Double totalBalance;

    public TotalBalanceAccountDTO(List<Account> list, Double totalBalance) {
        this.accountList = list;
        this.totalBalance = totalBalance;
    }
}
