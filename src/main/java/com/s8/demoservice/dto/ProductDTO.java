package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO {
    private Map<String, ProductVersionDTO> product_versions;
}
