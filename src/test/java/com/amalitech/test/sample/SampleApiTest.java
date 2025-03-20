package com.amalitech.test.sample;

import com.amalitech.test.base.BaseTest;
import com.amalitech.test.utils.ApiUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.amalitech.test.utils.WireMockUtils.stubGetWithJsonResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SampleApiTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(SampleApiTest.class);
    private WireMockServer wireMockServer;

    @BeforeMethod
    @Override
    public void setupMethod() {
        super.setupMethod(); // Call parent setup first

        // Get WireMock server instance if using mock server
        wireMockServer = getWireMockServer();
        if (wireMockServer != null) {
            log.info("Using WireMock server on port: {}", wireMockServer.port());
        } else {
            log.info("Using real server, WireMock not available");
        }

        // Example of how to switch to real server for specific tests
        // Uncomment the line below to use a real server
        // useRealServer("http://real-server-url");
    }

    @Test
    public void testSuccessfulGetRequest() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        String endpoint = "/api/users/1";
        String responseBody = "{ \"id\": 1, \"name\": \"John Doe\", \"email\": \"john@example.com\" }";

        stubGetWithJsonResponse(wireMockServer, endpoint, responseBody, 200);
        log.info("Stubbed GET request to {} with 200 response", endpoint);

        // Act
        log.info("Sending request to endpoint: {}", endpoint);
        Response response = ApiUtils.performGetRequest(requestSpec, endpoint);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("name")).isEqualTo("John Doe");
        assertThat(response.jsonPath().getString("email")).isEqualTo("john@example.com");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo(endpoint)));
    }

    @Test
    public void testSuccessfulPostRequest() {
        // Arrange
        String endpoint = "/api/users";
        String requestBody = "{ \"name\": \"Jane Smith\", \"email\": \"jane@example.com\" }";
        String responseBody = "{ \"id\": 2, \"name\": \"Jane Smith\", \"email\": \"jane@example.com\" }";

        wireMockServer.stubFor(post(urlEqualTo(endpoint))
                .withRequestBody(equalToJson(requestBody))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, endpoint, requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getInt("id")).isEqualTo(2);
        assertThat(response.jsonPath().getString("name")).isEqualTo("Jane Smith");

        // Verify the request was made with the correct body
        verify(postRequestedFor(urlEqualTo(endpoint))
                .withRequestBody(equalToJson(requestBody)));
    }

    @Test
    public void testNotFoundError() {
        // Arrange
        String endpoint = "/api/users/999";

        wireMockServer.stubFor(get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"error\": \"User not found\" }")));

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, endpoint);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("error")).isEqualTo("User not found");
    }
}
