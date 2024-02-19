package com.s8.demoservice.EventKafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.s8.demoservice.config.deserializer.InstantDeserializer;
import com.s8.demoservice.dto.TransactionDTO;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private List<TransactionDTO> posting_instruction_batches;
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Jakarta")
    private Instant valueTimestamp;
}
