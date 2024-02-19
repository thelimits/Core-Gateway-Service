package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.s8.demoservice.model.CustomerKYC;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountCustomerResponseDTO {
    private int number;
    private long totalElements;
    private int totalPages;
    private Pageable nextPageable;
    private Pageable previousPageable;
    private List<AccountDTO> customerAccount;

    public AccountCustomerResponseDTO(Page<CustomerKYC> customerPage, List<AccountDTO> account) {
        this.number = customerPage.getNumber();
        this.totalElements = customerPage.getTotalElements();
        this.totalPages = customerPage.getTotalPages() - 1;
        this.nextPageable = customerPage.nextPageable();
        this.previousPageable = customerPage.previousPageable();
        this.customerAccount = account;
    }
}
