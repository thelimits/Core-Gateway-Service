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
public class ParameterDTO {
    private String name;
    private String display_name;
    private String description;
    private String value;
    private String default_value;
    private Instant effective_timestamp;
    private String level;
    private String update_permission;
    private NumberParamDTO number;
    private ValuesParamDTO values;
}
