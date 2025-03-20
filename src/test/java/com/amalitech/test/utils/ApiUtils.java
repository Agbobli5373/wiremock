package com.amalitech.test.utils;

import com.amalitech.test.config.TestConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

public class ApiUtils {
    private static final Logger log = LoggerFactory.getLogger(ApiUtils.class);

    /**
     * Perform a GET request
     */
    public static Response performGetRequest(RequestSpecification requestSpec, String endpoint) {
        String baseUrl = TestConfig.getBaseUrl();
        log.info("Performing GET request to: {} (baseUri: {})", endpoint, baseUrl);

        RequestSpecification spec = given();

        if (requestSpec != null) {
            spec = spec.spec(requestSpec);
        }

        try {
            // Log the full URL being used
            log.info("Full request URL: {}{}", baseUrl, endpoint);

            return spec
                    .when()
                    .get(endpoint)
                    .then()
                    .extract()
                    .response();
        } catch (Exception e) {
            log.error("Error performing GET request: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Perform a POST request with a JSON body
     */
    public static Response performPostRequest(RequestSpecification requestSpec, String endpoint, Object bodyPayload) {
        String baseUri = TestConfig.getBaseUrl();
        log.info("Performing POST request to: {} (baseUri: {})", endpoint, baseUri);

        RequestSpecification spec = given();

        if (requestSpec != null) {
            spec = spec.spec(requestSpec);
        }

        try {
            // Log the full URL being used
            log.info("Full request URL: {}{}", baseUri, endpoint);

            return spec.body(bodyPayload)
                    .when()
                    .post(endpoint)
                    .then()
                    .extract()
                    .response();
        } catch (Exception e) {
            log.error("Error performing POST request: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Perform a PUT request with a JSON body
     */
    public static Response performPutRequest(RequestSpecification requestSpec, String endpoint, Object bodyPayload) {
        String baseUri = TestConfig.getBaseUrl();
        log.info("Performing PUT request to: {} (baseUri: {})", endpoint, baseUri);

        RequestSpecification spec = given();

        if (requestSpec != null) {
            spec = spec.spec(requestSpec);
        }

        try {
            // Log the full URL being used
            log.info("Full request URL: {}{}", baseUri, endpoint);

            return spec.body(bodyPayload)
                    .when()
                    .put(endpoint)
                    .then()
                    .extract()
                    .response();
        } catch (Exception e) {
            log.error("Error performing PUT request: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Perform a DELETE request
     */
    public static Response performDeleteRequest(RequestSpecification requestSpec, String endpoint) {
        String baseUri = TestConfig.getBaseUrl();
        log.info("Performing DELETE request to: {} (baseUri: {})", endpoint, baseUri);

        RequestSpecification spec = given();

        if (requestSpec != null) {
            spec = spec.spec(requestSpec);
        }

        try {
            // Log the full URL being used
            log.info("Full request URL: {}{}", baseUri, endpoint);

            return spec
                    .when()
                    .delete(endpoint)
                    .then()
                    .extract()
                    .response();
        } catch (Exception e) {
            log.error("Error performing DELETE request: {}", e.getMessage(), e);
            throw e;
        }
    }
}