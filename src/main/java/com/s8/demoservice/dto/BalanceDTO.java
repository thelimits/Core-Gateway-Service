package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.s8.demoservice.config.deserializer.InstantDeserializer;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceDTO {
    private String id;
    private String account_id;
    private String account_address;
    private String phase;
    private String asset;
    private String denomination;
    private String posting_instruction_batch_id;
    private String update_posting_instruction_batch_id;
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Jakarta")
    private Instant value_time;
    private Double amount;
    private Double total_debit;
    private Double total_credit;
}
