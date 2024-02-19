package com.s8.demoservice.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GenerateAccountNumber {
    private static final String TIMESTAMP_FORMAT = "HHmmssSSS";

    private static final int RANDOM_STRING_LENGTH = 3;

    private static final String RANDOM_STRING_CHARS = "0123456789";

    private static final SecureRandom secureRandom = new SecureRandom();

    private static final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);
    public String Generate(){
        String id = "5250";

        // generate timestamp
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(timestampFormatter);
        id += timestamp;

        // generate string random
        String randomString = generateRandomString(RANDOM_STRING_LENGTH);
        id += randomString;

        return id;
    }

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(RANDOM_STRING_CHARS.length());
            char randomChar = RANDOM_STRING_CHARS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }
}
