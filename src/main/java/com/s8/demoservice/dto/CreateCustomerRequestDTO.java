package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.s8.demoservice.model.CustomerKYC;
import com.s8.demoservice.model.enums.CustomerStatusType;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateCustomerRequestDTO {
    private String id;

    private String email;

    private String phoneNumber;

    private CustomerStatusType status;


    public CreateCustomerRequestDTO(CustomerKYC customer) {
        this.id = customer.getId();
        this.email = customer.getEmail();
        this.phoneNumber = customer.getPhoneNumber();
        this.status = customer.getStatus();
    }
}
