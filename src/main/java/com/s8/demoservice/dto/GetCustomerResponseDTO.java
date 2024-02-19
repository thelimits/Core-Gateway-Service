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
public class GetCustomerResponseDTO {

    private int number;
    private long totalElements;
    private int totalPages;
    private Pageable nextPageable;
    private Pageable previousPageable;
    private List<CustomerKYC> customers;

    public GetCustomerResponseDTO(Page<CustomerKYC> customerPage) {
        this.number = customerPage.getNumber();
        this.totalElements = customerPage.getTotalElements();
        this.totalPages = customerPage.getTotalPages() - 1;
        this.nextPageable = customerPage.nextPageable();
        this.previousPageable = customerPage.previousPageable();
        this.customers = customerPage.getContent();
    }
}
