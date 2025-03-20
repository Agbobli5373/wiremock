package com.amalitech.test.server;

import io.restassured.specification.RequestSpecification;

/**
 * Interface for server factory implementations
 */
public interface ServerFactory {
    /**
     * Initialize and start the server
     */
    void initialize();

    /**
     * Reset the server state (for mock servers)
     */
    void reset();

    /**
     * Shutdown the server
     */
    void shutdown();

    /**
     * Get the base URL of the server
     * 
     * @return The base URL string
     */
    String baseUrl();

    /**
     * Create a request specification for REST-assured
     * 
     * @return A configured RequestSpecification
     */
    RequestSpecification createRequestSpec();

    /**
     * Check if this factory manages a mock server
     * 
     * @return true if mock server, false if real server
     */
    boolean isMockServer();
}
