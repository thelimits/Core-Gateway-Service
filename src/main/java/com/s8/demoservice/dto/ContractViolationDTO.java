package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractViolationDTO {
    @JsonIgnore
    private String account_id;
    @JsonIgnore
    private String type;
    private String reason;
}
