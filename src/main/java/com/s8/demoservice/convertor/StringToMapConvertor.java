package com.s8.demoservice.convertor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class StringToMapConvertor implements Convertor<Map, String> {
    @Override
    public Map convert(final String input) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(input, Map.class);
    }
}
