package com.amalitech.test.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockUtils {
    private static final Logger log = LoggerFactory.getLogger(WireMockUtils.class);

    /**
     * Stub a GET request with a JSON response
     */
    public static void stubGetWithJsonResponse(WireMockServer server, String url, String jsonResponseBody,
            int statusCode) {
        log.info("Stubbing GET request for URL: {}", url);
        server.stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponseBody)
                        .withStatus(statusCode)));
    }

    /**
     * Stub a POST request with a JSON response
     */
    public static void stubPostWithJsonResponse(WireMockServer server, String url, String requestBody,
            String jsonResponseBody, int statusCode) {
        log.info("Stubbing POST request for URL: {}", url);
        server.stubFor(post(urlEqualTo(url))
                .withRequestBody(equalToJson(requestBody, true, true))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponseBody)
                        .withStatus(statusCode)));
    }

    /**
     * Load JSON from a file
     */
    public static String loadJsonFromFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            log.error("Failed to load JSON from file: {}", filePath, e);
            throw new RuntimeException("Could not load JSON from file: " + filePath, e);
        }
    }
}
