package com.amalitech.test.utils;

import io.restassured.RestAssured;
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
        int port =  RestAssured.port;
        String baseUri = RestAssured.baseURI;
        log.info("Performing GET request to: {} (port: {}, baseUri: {})",
                endpoint, port, baseUri);

        RequestSpecification spec = given();

        if (requestSpec != null) {
            spec = spec.spec(requestSpec);
        }

        // Always explicitly set the port to avoid any defaults
        spec = spec.port(port);

        try {
            // Log the full URL being used
            log.info("Full request URL: {}{}{}",
                    baseUri,
                    port > 0 ? ":" + port : "",
                    endpoint);

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
        int port = RestAssured.port;
        String baseUri = RestAssured.baseURI;
        log.info("Performing POST request to: {} (port: {}, baseUri: {})",
                endpoint, port, baseUri);

        RequestSpecification spec = given();

        if (requestSpec != null) {
            spec = spec.spec(requestSpec);
        }

        // Always explicitly set the port to avoid any defaults
        spec = spec.port(port);

        try {
            // Log the full URL being used
            log.info("Full request URL: {}{}{}",
                    baseUri,
                    port > 0 ? ":" + port : "",
                    endpoint);

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
        int port =  RestAssured.port;
        String baseUri = RestAssured.baseURI;
        log.info("Performing PUT request to: {} (port: {}, baseUri: {})",
                endpoint, port, baseUri);

        RequestSpecification spec = given();

        if (requestSpec != null) {
            spec = spec.spec(requestSpec);
        }

        // Always explicitly set the port to avoid any defaults
        spec = spec.port(port);

        try {
            // Log the full URL being used
            log.info("Full request URL: {}{}{}",
                    baseUri,
                    port > 0 ? ":" + port : "",
                    endpoint);

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
        int port = RestAssured.port;
        String baseUri =  RestAssured.baseURI;
        log.info("Performing DELETE request to: {} (port: {}, baseUri: {})",
                endpoint, port, baseUri);

        RequestSpecification spec = given();

        if (requestSpec != null) {
            spec = spec.spec(requestSpec);
        }

        // Always explicitly set the port to avoid any defaults
        spec = spec.port(port);

        try {
            // Log the full URL being used
            log.info("Full request URL: {}{}{}",
                    baseUri,
                    port > 0 ? ":" + port : "",
                    endpoint);

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