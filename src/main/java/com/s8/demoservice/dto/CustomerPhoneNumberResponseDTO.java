package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerPhoneNumberResponseDTO {
    private String phoneNumber;

    private String status;
}

