package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceParamDTO {
    private String account_tier_names;
    private String inactivity_fee_application_day = "1";
    private String interest_application_day = "1";
    private String maintenance_fee_application_day = "1";
    private String daily_withdrawal_limit_by_transaction_type;
}
