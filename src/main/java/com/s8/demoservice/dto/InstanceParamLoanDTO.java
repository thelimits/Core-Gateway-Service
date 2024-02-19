package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceParamLoanDTO {
    //    loan account without balloon payment
    private Double fixed_interest_rate = 0.1;
    private Double upfront_fee = 0.00;
    private String amortise_upfront_fee = "False";
    private String fixed_interest_loan = "True";
    private Integer total_term = 12;
    private Double principal = 1000000.00;
    private Integer repayment_day = 28;
    private String deposit_account;
    private Double variable_rate_adjustment = 0.00;
    private String interest_accrual_rest_type = "daily";
    private String capitalise_late_repayment_fee = "False";
    private String repayment_holiday_impact_preference = "increase_emi";
    //    if loan account with balloon payment
    private String amortisation_method_no_balloon = "declining_principal";
    private String amortisation_method_with_balloon = "minimum_repayment_with_balloon_payment";
    private Double balloon_payment_amount;
    private Integer balloon_payment_days_delta = 5;
}
