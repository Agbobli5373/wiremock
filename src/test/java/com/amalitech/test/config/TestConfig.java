package com.amalitech.test.config;

public class TestConfig {
    private static String baseUrl;

    public static void setBaseUrl(String baseUrl) {
        TestConfig.baseUrl = baseUrl;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }
}
