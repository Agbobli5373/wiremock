package com.amalitech.test.ecommerce;

import com.amalitech.test.base.BaseTest;
import com.amalitech.test.model.Cart;
import com.amalitech.test.model.CartItem;
import com.amalitech.test.utils.ApiUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CartApiTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(CartApiTest.class);
    private WireMockServer wireMockServer;
    private String authToken = "Bearer mock-jwt-token";
    private String cartId;

    @BeforeClass
    public void setUp() {
        super.setupClass();
        wireMockServer = getWireMockServer();
        cartId = UUID.randomUUID().toString();
        setupCartStubs();
    }

    private void setupCartStubs() {
        if (wireMockServer == null)
            return;

        // Stub for getting cart
        wireMockServer.stubFor(get(urlPathEqualTo("/api/cart"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"id\": \"" + cartId + "\",\n" +
                                "  \"userId\": \"user-123\",\n" +
                                "  \"items\": [\n" +
                                "    {\n" +
                                "      \"productId\": 1,\n" +
                                "      \"productName\": \"Smartphone\",\n" +
                                "      \"quantity\": 1,\n" +
                                "      \"unitPrice\": 599.99,\n" +
                                "      \"totalPrice\": 599.99\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"productId\": 2,\n" +
                                "      \"productName\": \"Wireless Headphones\",\n" +
                                "      \"quantity\": 2,\n" +
                                "      \"unitPrice\": 149.99,\n" +
                                "      \"totalPrice\": 299.98\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"subtotal\": 899.97,\n" +
                                "  \"tax\": 90.00,\n" +
                                "  \"total\": 989.97\n" +
                                "}")));

        // Stub for empty cart
        wireMockServer.stubFor(get(urlPathEqualTo("/api/cart/empty"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"id\": \"" + cartId + "\",\n" +
                                "  \"userId\": \"user-123\",\n" +
                                "  \"items\": [],\n" +
                                "  \"subtotal\": 0,\n" +
                                "  \"tax\": 0,\n" +
                                "  \"total\": 0\n" +
                                "}")));

        // Stub for adding item to cart
        wireMockServer.stubFor(post(urlPathEqualTo("/api/cart/items"))
                .withHeader("Authorization", matching("Bearer .*"))
                .withRequestBody(matchingJsonPath("$.productId"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"id\": \"" + cartId + "\",\n" +
                                "  \"userId\": \"user-123\",\n" +
                                "  \"items\": [\n" +
                                "    {\n" +
                                "      \"productId\": 1,\n" +
                                "      \"productName\": \"Smartphone\",\n" +
                                "      \"quantity\": 1,\n" +
                                "      \"unitPrice\": 599.99,\n" +
                                "      \"totalPrice\": 599.99\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"subtotal\": 599.99,\n" +
                                "  \"tax\": 60.00,\n" +
                                "  \"total\": 659.99\n" +
                                "}")));

        // Stub for updating cart item quantity
        wireMockServer.stubFor(put(urlPathMatching("/api/cart/items/\\d+"))
                .withHeader("Authorization", matching("Bearer .*"))
                .withRequestBody(matchingJsonPath("$.quantity"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"id\": \"" + cartId + "\",\n" +
                                "  \"userId\": \"user-123\",\n" +
                                "  \"items\": [\n" +
                                "    {\n" +
                                "      \"productId\": 1,\n" +
                                "      \"productName\": \"Smartphone\",\n" +
                                "      \"quantity\": 2,\n" +
                                "      \"unitPrice\": 599.99,\n" +
                                "      \"totalPrice\": 1199.98\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"subtotal\": 1199.98,\n" +
                                "  \"tax\": 120.00,\n" +
                                "  \"total\": 1319.98\n" +
                                "}")));

        // Stub for removing item from cart
        wireMockServer.stubFor(delete(urlPathMatching("/api/cart/items/\\d+"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"id\": \"" + cartId + "\",\n" +
                                "  \"userId\": \"user-123\",\n" +
                                "  \"items\": [],\n" +
                                "  \"subtotal\": 0,\n" +
                                "  \"tax\": 0,\n" +
                                "  \"total\": 0\n" +
                                "}")));

        // Stub for clearing the cart
        wireMockServer.stubFor(delete(urlPathEqualTo("/api/cart"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"id\": \"" + cartId + "\",\n" +
                                "  \"userId\": \"user-123\",\n" +
                                "  \"items\": [],\n" +
                                "  \"subtotal\": 0,\n" +
                                "  \"tax\": 0,\n" +
                                "  \"total\": 0\n" +
                                "}")));
    }

    @Test
    public void testGetCart() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/cart");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        Cart cart = response.as(Cart.class);
        assertThat(cart).isNotNull();
        assertThat(cart.getId()).isEqualTo(cartId);
        assertThat(cart.getItems()).hasSize(2);
        assertThat(cart.getTotal()).isGreaterThan(BigDecimal.ZERO);

        // Verify first item
        CartItem firstItem = cart.getItems().get(0);
        assertThat(firstItem.getProductId()).isEqualTo(1L);
        assertThat(firstItem.getProductName()).isEqualTo("Smartphone");

        // Verify the request was made
        verify(getRequestedFor(urlPathEqualTo("/api/cart"))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testAddItemToCart() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        String requestBody = "{\n" +
                "  \"productId\": 1,\n" +
                "  \"quantity\": 1\n" +
                "}";
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/cart/items", requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> items = jsonPath.getList("items");

        assertThat(items).hasSize(1);
        assertThat(jsonPath.getString("items[0].productName")).isEqualTo("Smartphone");
        assertThat(jsonPath.getInt("items[0].quantity")).isEqualTo(1);

        // Verify the request was made
        verify(postRequestedFor(urlPathEqualTo("/api/cart/items"))
                .withHeader("Authorization", equalTo(authToken))
                .withRequestBody(matchingJsonPath("$.productId")));
    }

    @Test
    public void testUpdateCartItemQuantity() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        String requestBody = "{\n" +
                "  \"quantity\": 2\n" +
                "}";
        Long productId = 1L;
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performPutRequest(requestSpec, "/api/cart/items/" + productId, requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();

        // Check that quantity was updated
        assertThat(jsonPath.getInt("items[0].quantity")).isEqualTo(2);

        // Check that total price was recalculated
        BigDecimal totalPrice = new BigDecimal(jsonPath.getString("items[0].totalPrice"));
        BigDecimal unitPrice = new BigDecimal(jsonPath.getString("items[0].unitPrice"));

        assertThat(totalPrice).isEqualByComparingTo(unitPrice.multiply(BigDecimal.valueOf(2)));

        // Verify the subtotal and total were updated
        BigDecimal subtotal = new BigDecimal(jsonPath.getString("subtotal"));
        assertThat(subtotal).isEqualByComparingTo(totalPrice);

        // Verify the request was made
        verify(putRequestedFor(urlPathMatching("/api/cart/items/\\d+"))
                .withHeader("Authorization", equalTo(authToken))
                .withRequestBody(matchingJsonPath("$.quantity")));
    }

    @Test
    public void testRemoveItemFromCart() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        Long productId = 1L;
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performDeleteRequest(requestSpec, "/api/cart/items/" + productId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> items = jsonPath.getList("items");

        // Cart should be empty
        assertThat(items).isEmpty();

        // Totals should be zero
        assertThat(jsonPath.getDouble("subtotal")).isZero();
        assertThat(jsonPath.getDouble("tax")).isZero();
        assertThat(jsonPath.getDouble("total")).isZero();

        // Verify the request was made
        verify(deleteRequestedFor(urlPathMatching("/api/cart/items/\\d+"))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testClearCart() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performDeleteRequest(requestSpec, "/api/cart");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> items = jsonPath.getList("items");

        // Cart should be empty
        assertThat(items).isEmpty();

        // Totals should be zero
        assertThat(jsonPath.getDouble("subtotal")).isZero();

        // Verify the request was made
        verify(deleteRequestedFor(urlPathEqualTo("/api/cart"))
                .withHeader("Authorization", equalTo(authToken)));
    }
}
