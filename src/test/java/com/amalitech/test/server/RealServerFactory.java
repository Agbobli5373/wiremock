package com.amalitech.test.server;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.config.RestAssuredConfig.config;

/**
 * Factory for connecting to real server
 */
public class RealServerFactory implements ServerFactory {
    private static final Logger log = LoggerFactory.getLogger(RealServerFactory.class);
    private String baseUrl;

    /**
     * Constructor with base URL
     * 
     * @param baseUrl Full URL of the real server
     */
    public RealServerFactory(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void initialize() {
        log.info("Configuring real server connection to: {}", baseUrl);

        // Configure RestAssured for real server
        RestAssured.reset();
        RestAssured.baseURI = baseUrl;
        RestAssured.urlEncodingEnabled = false;

        log.info("RestAssured configured with baseURI: {}", RestAssured.baseURI);

        RestAssured.config = config()
                .encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
    }

    @Override
    public void reset() {
        // No reset needed for real server
        log.info("Reset operation not applicable for real server");
    }

    @Override
    public void shutdown() {
        // No shutdown needed for real server
        log.info("Shutdown operation not applicable for real server");
    }

    @Override
    public String baseUrl() {
        return baseUrl;
    }

    @Override
    public RequestSpecification createRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setUrlEncodingEnabled(false)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    @Override
    public boolean isMockServer() {
        return false;
    }
}
