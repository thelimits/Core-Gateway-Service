package com.s8.demoservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name="AccountAdditionalDetails")
public class AccountAdditionalDetails {
    @Id
    @JsonIgnore
    private String id;

    private String sourceOfFunds;
    private String workPlace;
    private String rangeSalaries;
    private String purpose;
}
