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
public class ProductVersionDTO {
    private String id;
    private String display_name;
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Asia/Jakarta")
    private Instant create_timestamp;
    private boolean is_current;
    private String summary;
    private List<ParameterDTO> params;
    private String product_id;
    private String tside;
    private List<String> supported_denominations;
}
