package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceParamBnplDTO {
    Double principal;
    String deposit_account;
    Integer total_repayment_count;
    String repayment_frequency = "monthly";
}
