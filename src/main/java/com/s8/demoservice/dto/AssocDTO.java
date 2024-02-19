package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssocDTO {
    private String id;
    private String account_id;
    private String schedule_id;
    private Instant create_timestamp;
}
