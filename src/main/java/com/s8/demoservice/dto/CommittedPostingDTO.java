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
public class CommittedPostingDTO {
    private Boolean credit;
    private Double amount;
    private String denomination;
    private String account_id;
    private String account_address;
    private String asset;
    private String phase;
}
