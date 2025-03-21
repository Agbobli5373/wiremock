package com.amalitech.test.ecommerce;

import com.amalitech.test.base.BaseTest;
import com.amalitech.test.utils.ApiUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductApiTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(ProductApiTest.class);
    private WireMockServer wireMockServer;

    @BeforeClass
    public void setUp() {
        super.setupClass();
        wireMockServer = getWireMockServer();
        setupProductStubs();
    }

    private void setupProductStubs() {
        if (wireMockServer == null)
            return;

        // Stub for getting all products
        wireMockServer.stubFor(get(urlEqualTo("/api/products"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/all_products.json")));

        // Stub for product search
        wireMockServer.stubFor(get(urlPathEqualTo("/api/products/search"))
                .withQueryParam("query", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/search_results.json")));

        // Stub for product categories
        wireMockServer.stubFor(get(urlEqualTo("/api/products/categories"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"categories\": [\"Electronics\", \"Clothing\", \"Books\", \"Home & Kitchen\"]}")));

        // Stub for product details (dynamic path parameter)
        wireMockServer.stubFor(get(urlMatching("/api/products/\\d+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/product_details.json")));

        // Stub for product not found
        wireMockServer.stubFor(get(urlEqualTo("/api/products/999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Product not found\"}")));

        // Stub for product reviews
        wireMockServer.stubFor(get(urlMatching("/api/products/\\d+/reviews"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/product_reviews.json")));

        // Setup additional product-related stubs as needed
    }

    @Test
    public void testGetAllProducts() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/products");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Parse the response body
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> products = jsonPath.getList("products");

        // Verify the response contains products
        assertThat(products).isNotNull();
        assertThat(products).isNotEmpty();
        assertThat(products.size()).isGreaterThanOrEqualTo(2);

        // Verify the structure of the first product
        Map<String, Object> firstProduct = products.get(0);
        assertThat(firstProduct).containsKeys("id", "name", "price", "category");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/products")));
    }

    @Test
    public void testSearchProducts() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/products/search?query=phone");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Parse the response body
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> results = jsonPath.getList("results");

        // Verify the results
        assertThat(results).isNotNull();
        assertThat(results).isNotEmpty();

        // Verify the request was made
        verify(getRequestedFor(urlPathEqualTo("/api/products/search"))
                .withQueryParam("query", WireMock.equalTo("phone")));
    }

    @Test
    public void testGetProductDetails() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Act
        Long productId = 1L;
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/products/" + productId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Parse the response body
        JsonPath jsonPath = response.jsonPath();
        Map<String, Object> product = jsonPath.getMap("$");

        // Verify the product details
        assertThat(product).isNotNull();
        assertThat(product).containsKeys("id", "name", "description", "price", "stockQuantity", "category");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/products/" + productId)));
    }

    @Test
    public void testProductNotFound() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/products/999");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(404);

        // Parse the response body
        JsonPath jsonPath = response.jsonPath();
        String error = jsonPath.getString("error");

        // Verify the error message
        assertThat(error).isEqualTo("Product not found");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/products/999")));
    }

    @Test
    public void testGetProductCategories() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/products/categories");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Parse the response body
        JsonPath jsonPath = response.jsonPath();
        List<String> categories = jsonPath.getList("categories");

        // Verify the categories
        assertThat(categories).isNotNull();
        assertThat(categories).containsExactly("Electronics", "Clothing", "Books", "Home & Kitchen");

        // Verify the request was made
        verify(getRequestedFor(urlEqualTo("/api/products/categories")));
    }
}
