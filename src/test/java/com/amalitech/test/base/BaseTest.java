package com.amalitech.test.base;

import com.amalitech.test.config.TestConfig;
import com.amalitech.test.server.ServerFactory;
import com.amalitech.test.server.ServerFactoryProvider;
import com.amalitech.test.server.WireMockServerFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {
    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    protected static ServerFactory serverFactory;
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;
    private WireMockServer wireMockServer;

    protected WireMockServer getWireMockServer() {
        if (serverFactory instanceof WireMockServerFactory) {
            return ((WireMockServerFactory) serverFactory).getWireMockServer();
        }

        // If the WireMock server is null, create it with configuration from classpath
        // resources
        if (wireMockServer == null) {
            log.info("Creating new WireMock server with mappings from classpath resources");

            try {
                WireMockConfiguration config = WireMockConfiguration.options()
                        .port(8080) // Use the default port or get from properties
                        .usingFilesUnderClasspath("src/test/resources"); // Important: This tells WireMock to look for
                                                                         // mappings in the classpath

                wireMockServer = new WireMockServer(config);
                wireMockServer.start();
                log.info("WireMock server started with configuration from classpath resources");

                // Log mapping info for debugging
                log.info("WireMock mappings loaded: {}", wireMockServer.getStubMappings().size());
                wireMockServer.getStubMappings().forEach(mapping -> log.debug("Loaded mapping: {}",
                        mapping.getName() != null ? mapping.getName() : mapping.getRequest().getUrl()));
            } catch (Exception e) {
                log.error("Failed to start WireMock server with classpath resources", e);
            }
        }

        return wireMockServer;
    }

    @BeforeSuite
    public void setupServerFactory() {
        // By default, use mock server - can be changed by test configurations
        serverFactory = ServerFactoryProvider.getMockServerFactory();
        serverFactory.initialize();
        TestConfig.setBaseUrl(serverFactory.baseUrl());
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

        // Reset mock server if applicable
        if (serverFactory.isMockServer()) {
            serverFactory.reset();
        }
    }

    private void initializeSpecifications() {
        // Create request specification from server factory
        requestSpec = serverFactory.createRequestSpec();
        log.info("Using server at: {}", serverFactory.baseUrl());
    }

    /**
     * Switch to a real server for tests
     *
     * @param realServerUrl URL of the real server
     */
    public  void useRealServer(String realServerUrl) {
        // Clean up existing factory if needed
        if (serverFactory != null) {
            serverFactory.shutdown();
        }

        log.info("Switching to real server: {}", realServerUrl);
        serverFactory = ServerFactoryProvider.getRealServerFactory(realServerUrl);
        serverFactory.initialize();
        TestConfig.setBaseUrl(serverFactory.baseUrl());
    }

    /**
     * Switch to a mock server for tests
     */
    public void useMockServer() {
        // Clean up existing factory if needed
        if (serverFactory != null) {
            serverFactory.shutdown();
        }

        log.info("Switching to mock server");
        serverFactory = ServerFactoryProvider.getMockServerFactory();
        serverFactory.initialize();
        TestConfig.setBaseUrl(serverFactory.baseUrl());
    }

    @AfterClass
    public void tearDownTest() {
        log.info("Tearing down test");
        if (serverFactory.isMockServer()) {
            serverFactory.reset();
        }
    }

    @AfterSuite
    public  void tearDownServer() {
        log.info("Shutting down server");
        if (serverFactory != null) {
            serverFactory.shutdown();
        }
    }
}