package com.example.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base test class for setting up RestAssured configuration.
 */
public class BaseTest {
    @BeforeAll
    public static void setup() {
        // Base URL is configurable via system property or environment variable
        String baseUrl = System.getProperty("BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = System.getenv().getOrDefault("BASE_URL", "http://localhost:3000");
        }
        RestAssured.baseURI = baseUrl;
    }
}
