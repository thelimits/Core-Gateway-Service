package com.s8.demoservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class StringUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String convertMapToQueryParamString(Map<String, Object> queryParamMap) {
        StringBuilder queryString = new StringBuilder("");
        for (String key : queryParamMap.keySet()) {
            queryString.append(key + "=" + queryParamMap.get(key) + "&");
        }
        queryString.deleteCharAt(queryString.length()-1);
        return queryString.toString();
    }

    public static Map convertJsonStringToMap(String jsonString) throws Exception{
        Map<String, Object> map
                = objectMapper.readValue(jsonString, new TypeReference<Map<String,Object>>(){});
        return map;
    }
}
