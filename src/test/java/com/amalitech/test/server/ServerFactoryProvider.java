package com.amalitech.test.server;

/**
 * Provider class that creates appropriate server factory instances
 */
public class ServerFactoryProvider {
    private static ServerFactory currentFactory;

    /**
     * Get a mock server factory
     * 
     * @return WireMockServerFactory instance
     */
    public static ServerFactory getMockServerFactory() {
        currentFactory = new WireMockServerFactory();
        return currentFactory;
    }

    /**
     * Get a mock server factory with specific port
     * 
     * @param port Port to use
     * @return WireMockServerFactory instance
     */
    public static ServerFactory getMockServerFactory(int port) {
        currentFactory = new WireMockServerFactory(port);
        return currentFactory;
    }

    /**
     * Get a real server factory
     * 
     * @param baseUrl URL of the real server
     * @return RealServerFactory instance
     */
    public static ServerFactory getRealServerFactory(String baseUrl) {
        currentFactory = new RealServerFactory(baseUrl);
        return currentFactory;
    }

    /**
     * Get the currently active server factory
     * 
     * @return Current ServerFactory or null if none is set
     */
    public static ServerFactory getCurrentFactory() {
        return currentFactory;
    }
}
