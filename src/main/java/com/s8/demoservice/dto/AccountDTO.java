package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.s8.demoservice.model.Account;
import com.s8.demoservice.model.CustomerKYC;
import lombok.*;

import java.sql.Date;
import java.util.List;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String nik;
    private String email;
    private String number;
    private Date dob;
    private String address;
    private List<Account> account;

    public AccountDTO(CustomerKYC cust, List<Account> account){
        this.id = cust.getId();
        this.firstName = cust.getFirstName();
        this.lastName = cust.getLastName();
        this.nik = cust.getNik();
        this.email = cust.getEmail();
        this.number = cust.getPhoneNumber();
        this.dob = cust.getDob();
        this.address = cust.getAddress();
        this.account = account;
    }
}
