package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.s8.demoservice.model.enums.CustomerStatusType;
import lombok.*;

import java.sql.Date;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerAllResponseDTO {
    private String id;

    private String firstName;

    private String lastName;

    private String address;

    private String nik;

    private Date dob;

    private String motherMaidenName;

    private String email;

    private String pin;

    private String phoneNumber;

    private CustomerStatusType status;
}
