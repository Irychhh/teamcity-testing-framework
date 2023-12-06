package com.example.teamcity.api.generators;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomData {
    private static final int LENGTH = 10;
    private static final int CRITICAL_LENGTH = 226;

    public static String getString() {
        return "test_" + RandomStringUtils.randomAlphabetic(LENGTH);
    }
    public static String getCriticalLengthString() {
        return "test_" + RandomStringUtils.randomAlphabetic(CRITICAL_LENGTH);
    }
}
