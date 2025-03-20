package com.amalitech.test.server;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
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
 * Factory for creating and managing WireMock server
 */
public class WireMockServerFactory implements ServerFactory {
    private static final Logger log = LoggerFactory.getLogger(WireMockServerFactory.class);
    private WireMockServer wireMockServer;
    private int port;
    private String baseUrl;

    /**
     * Constructor with specified port
     * 
     * @param port Port to use for WireMock server or 0 for dynamic port
     */
    public WireMockServerFactory(int port) {
        this.port = port;
    }

    /**
     * Default constructor uses port 8080
     */
    public WireMockServerFactory() {
        this(8080);
    }

    @Override
    public void initialize() {
        log.info("Starting WireMock server");

        try {
            wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
            wireMockServer.start();
        } catch (Exception e) {
            log.warn("Could not start WireMock on port {}, using random port", port, e);
            wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
            wireMockServer.start();
        }

        port = wireMockServer.port();
        baseUrl = "http://localhost:" + port;

        log.info("WireMock server started on port: {}", port);

        // Configure RestAssured global settings
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "";
        RestAssured.urlEncodingEnabled = false;

        log.info("RestAssured configured with baseURI: {}, port: {}, basePath: '{}'",
                RestAssured.baseURI, RestAssured.port, RestAssured.basePath);

        RestAssured.config = config()
                .encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
    }

    @Override
    public void reset() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            log.info("Resetting WireMock server");
            wireMockServer.resetAll();
        }
    }

    @Override
    public void shutdown() {
        log.info("Stopping WireMock server");
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Override
    public String baseUrl() {
        return baseUrl;
    }

    /**
     * Get the WireMock server instance
     */
    public WireMockServer getWireMockServer() {
        return wireMockServer;
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
        return true;
    }
}
