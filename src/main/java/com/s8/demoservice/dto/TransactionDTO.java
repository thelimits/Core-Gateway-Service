package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.s8.demoservice.config.deserializer.InstantDeserializer;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDTO {
    private String id;
    private String client_batch_id;
    private String status;
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Jakarta")
    private Instant value_timestamp;
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Jakarta")
    private Instant insertion_timestamp;
    private List<PostingInstructionDTO> posting_instructions;
}
