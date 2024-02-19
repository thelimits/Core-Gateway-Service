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
public class JobsDTO {
    private String id;
    private String status;
    private String schedule_id;
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Jakarta")
    private Instant schedule_timestamp;
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Jakarta")
    private Instant publish_timestamp;
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Jakarta")
    private Instant completed_timestamp;
}
