package com.amalitech.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class JsonUtils {
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    /**
     * Loads a JSON file from the resources directory.
     *
     * @param path The path to the JSON file relative to the resources directory
     * @return The contents of the JSON file as a String, or null if the file could
     *         not be loaded
     */
    public static String loadJsonFromResources(String path) {
        log.debug("Attempting to load JSON from resources: {}", path);

        try (InputStream is = JsonUtils.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                log.error("Resource not found: {}", path);
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String json = reader.lines().collect(Collectors.joining("\n"));
                log.debug("Successfully loaded JSON from resources: {}", path);
                return json;
            }
        } catch (IOException e) {
            log.error("Failed to load JSON from resources: {}", path, e);
            return null;
        }
    }

    /**
     * Get JSON content from a resource, or return a fallback string if the resource
     * cannot be loaded
     * 
     * @param path     The path to the JSON file relative to the resources directory
     * @param fallback The fallback JSON string to use if the file cannot be loaded
     * @return The contents of the JSON file as a String, or the fallback string
     */
    public static String getJsonContentOrFallback(String path, String fallback) {
        String content = loadJsonFromResources(path);
        if (content != null) {
            return content;
        }
        log.warn("Using fallback JSON content for {}", path);
        return fallback;
    }
}