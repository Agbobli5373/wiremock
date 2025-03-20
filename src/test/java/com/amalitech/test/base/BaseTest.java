package com.amalitech.test.base;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeMethod;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.config.RestAssuredConfig.config;

public abstract class BaseTest {
    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    protected static WireMockServer wireMockServer;
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;

    @BeforeSuite
    public static void setupWireMock() {
        log.info("Starting WireMock server");

        try {
            wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8080));
            wireMockServer.start();
        } catch (Exception e) {
            log.warn("Could not start WireMock on port 8089, using random port", e);
            wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
            wireMockServer.start();
        }

        int port = wireMockServer.port();
        log.info("WireMock server started on port: {}", port);

        // Configure RestAssured global settings - VERY IMPORTANT to prevent connection
        RestAssured.reset(); // Reset any existing configuration
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "";

        // Set urlEncodingEnabled to false to prevent URL encoding issues
        RestAssured.urlEncodingEnabled = false;

        // Ensure this is logged
        log.info("RestAssured configured with baseURI: {}, port: {}, basePath: '{}'",
                RestAssured.baseURI, RestAssured.port, RestAssured.basePath);

        RestAssured.config = config()
                .encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
    }

    @BeforeClass
    public void setupClass() {
        log.info("Base test class setup");
        initializeSpecifications();
    }

    @BeforeMethod
    public void setupMethod() {
        log.info("Base test method setup");
        initializeSpecifications();
    }

    private void initializeSpecifications() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            setupWireMock();
        }

        int port = wireMockServer.port();
        // Update RestAssured port just to make sure it's still correct
        RestAssured.port = port;

        log.info("Using WireMock port: {}, RestAssured port: {}", port, RestAssured.port);

        // Setup request specification with explicit port
        requestSpec = new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(port)
                .setUrlEncodingEnabled(false) // Prevent URL encoding issues
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();

        // Make sure the request spec has proper port
        // log.info("RequestSpec baseURI: {}, port: {}", requestSpec.getBaseUri(), requestSpec.getPort());

        // Setup response specification
        responseSpec = new ResponseSpecBuilder()
                .expectContentType("application/json")
                .build();
    }

    @AfterClass
    public void tearDownTest() {
        log.info("Tearing down test");
        wireMockServer.resetAll();
    }

    @AfterSuite
    public static void tearDownWireMock() {
        log.info("Stopping WireMock server");
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }
}
