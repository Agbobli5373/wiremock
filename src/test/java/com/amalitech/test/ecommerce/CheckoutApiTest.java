package com.amalitech.test.ecommerce;

import com.amalitech.test.base.BaseTest;
import com.amalitech.test.model.Order;
import com.amalitech.test.utils.ApiUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CheckoutApiTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(CheckoutApiTest.class);
    private WireMockServer wireMockServer;
    private String authToken = "Bearer mock-jwt-token";
    private String cartId;
    private String orderId;

    @BeforeClass
    public void setUp() {
        super.setupClass();
        wireMockServer = getWireMockServer();
        cartId = UUID.randomUUID().toString();
        orderId = UUID.randomUUID().toString();
        setupCheckoutStubs();
    }

    private void setupCheckoutStubs() {
        if (wireMockServer == null)
            return;

        // Stub for initiating checkout
        wireMockServer.stubFor(post(urlPathEqualTo("/api/checkout"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"checkoutId\": \"checkout-123\",\n" +
                                "  \"cartId\": \"" + cartId + "\",\n" +
                                "  \"subtotal\": 599.99,\n" +
                                "  \"tax\": 60.00,\n" +
                                "  \"shipping\": 10.00,\n" +
                                "  \"total\": 669.99,\n" +
                                "  \"paymentMethods\": [\"Credit Card\", \"PayPal\", \"Apple Pay\"],\n" +
                                "  \"shippingMethods\": [\n" +
                                "    {\"id\": \"standard\", \"name\": \"Standard Shipping\", \"price\": 10.00, \"estimatedDays\": \"3-5\"},\n"
                                +
                                "    {\"id\": \"express\", \"name\": \"Express Shipping\", \"price\": 25.00, \"estimatedDays\": \"1-2\"}\n"
                                +
                                "  ]\n" +
                                "}")));

        // Stub for validating shipping address
        wireMockServer.stubFor(post(urlPathEqualTo("/api/checkout/shipping-address"))
                .withHeader("Authorization", matching("Bearer .*"))
                .withRequestBody(matchingJsonPath("$.address"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"valid\": true,\n" +
                                "  \"normalized\": {\n" +
                                "    \"street\": \"123 Main Street\",\n" +
                                "    \"city\": \"New York\",\n" +
                                "    \"state\": \"NY\",\n" +
                                "    \"zipCode\": \"10001\",\n" +
                                "    \"country\": \"USA\"\n" +
                                "  }\n" +
                                "}")));

        // Stub for selecting shipping method
        wireMockServer.stubFor(post(urlPathEqualTo("/api/checkout/shipping-method"))
                .withHeader("Authorization", matching("Bearer .*"))
                .withRequestBody(matchingJsonPath("$.shippingMethodId"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"checkoutId\": \"checkout-123\",\n" +
                                "  \"shippingMethod\": {\n" +
                                "    \"id\": \"express\",\n" +
                                "    \"name\": \"Express Shipping\",\n" +
                                "    \"price\": 25.00,\n" +
                                "    \"estimatedDays\": \"1-2\"\n" +
                                "  },\n" +
                                "  \"subtotal\": 599.99,\n" +
                                "  \"tax\": 60.00,\n" +
                                "  \"shipping\": 25.00,\n" +
                                "  \"total\": 684.99\n" +
                                "}")));

        // Stub for processing payment
        wireMockServer.stubFor(post(urlPathEqualTo("/api/checkout/payment"))
                .withHeader("Authorization", matching("Bearer .*"))
                .withRequestBody(matchingJsonPath("$.paymentMethod"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"transactionId\": \"txn-" + UUID.randomUUID().toString() + "\",\n" +
                                "  \"status\": \"success\",\n" +
                                "  \"orderId\": \"" + orderId + "\"\n" +
                                "}")));

        // Stub for retrieving order after checkout
        wireMockServer.stubFor(get(urlPathEqualTo("/api/orders/" + orderId))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"id\": \"" + orderId + "\",\n" +
                                "  \"userId\": \"user-123\",\n" +
                                "  \"status\": \"pending\",\n" +
                                "  \"createdAt\": \"2023-07-15T08:30:45Z\",\n" +
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
                                "  \"shipping\": 25.00,\n" +
                                "  \"total\": 684.99,\n" +
                                "  \"shippingAddress\": {\n" +
                                "    \"street\": \"123 Main Street\",\n" +
                                "    \"city\": \"New York\",\n" +
                                "    \"state\": \"NY\",\n" +
                                "    \"zipCode\": \"10001\",\n" +
                                "    \"country\": \"USA\"\n" +
                                "  },\n" +
                                "  \"paymentInfo\": {\n" +
                                "    \"method\": \"Credit Card\",\n" +
                                "    \"transactionId\": \"txn-789-xyz\",\n" +
                                "    \"status\": \"completed\"\n" +
                                "  }\n" +
                                "}")));
    }

    @Test
    public void testInitiateCheckout() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/checkout", "{}");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("checkoutId")).isEqualTo("checkout-123");
        assertThat(jsonPath.getString("cartId")).isEqualTo(cartId);

        // Verify payment methods
        assertThat(jsonPath.getList("paymentMethods")).contains("Credit Card", "PayPal", "Apple Pay");

        // Verify shipping methods
        assertThat(jsonPath.getList("shippingMethods")).hasSize(2);
        assertThat(jsonPath.getString("shippingMethods[0].id")).isEqualTo("standard");

        // Verify the request was made
        verify(postRequestedFor(urlPathEqualTo("/api/checkout"))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testValidateShippingAddress() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        String requestBody = "{\n" +
                "  \"address\": {\n" +
                "    \"street\": \"123 Main St\",\n" +
                "    \"city\": \"New York\",\n" +
                "    \"state\": \"NY\",\n" +
                "    \"zipCode\": \"10001\",\n" +
                "    \"country\": \"USA\"\n" +
                "  }\n" +
                "}";
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/checkout/shipping-address", requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getBoolean("valid")).isTrue();

        // Check normalized address
        assertThat(jsonPath.getString("normalized.street")).isEqualTo("123 Main Street");
        assertThat(jsonPath.getString("normalized.city")).isEqualTo("New York");

        // Verify the request was made
        verify(postRequestedFor(urlPathEqualTo("/api/checkout/shipping-address"))
                .withHeader("Authorization", equalTo(authToken))
                .withRequestBody(matchingJsonPath("$.address")));
    }

    @Test
    public void testSelectShippingMethod() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        String requestBody = "{\n" +
                "  \"shippingMethodId\": \"express\"\n" +
                "}";
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/checkout/shipping-method", requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("shippingMethod.id")).isEqualTo("express");

        // Total should include shipping cost
        BigDecimal subtotal = new BigDecimal(jsonPath.getString("subtotal"));
        BigDecimal tax = new BigDecimal(jsonPath.getString("tax"));
        BigDecimal shipping = new BigDecimal(jsonPath.getString("shipping"));
        BigDecimal total = new BigDecimal(jsonPath.getString("total"));

        assertThat(total).isEqualByComparingTo(subtotal.add(tax).add(shipping));

        // Verify the request was made
        verify(postRequestedFor(urlPathEqualTo("/api/checkout/shipping-method"))
                .withHeader("Authorization", equalTo(authToken))
                .withRequestBody(matchingJsonPath("$.shippingMethodId")));
    }

    @Test
    public void testProcessPayment() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        String requestBody = "{\n" +
                "  \"paymentMethod\": \"Credit Card\",\n" +
                "  \"cardNumber\": \"4111111111111111\",\n" +
                "  \"expiryMonth\": \"12\",\n" +
                "  \"expiryYear\": \"2025\",\n" +
                "  \"cvv\": \"123\"\n" +
                "}";
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/checkout/payment", requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("status")).isEqualTo("success");
        assertThat(jsonPath.getString("orderId")).isNotEmpty();

        // Verify the request was made
        verify(postRequestedFor(urlPathEqualTo("/api/checkout/payment"))
                .withHeader("Authorization", equalTo(authToken))
                .withRequestBody(matchingJsonPath("$.paymentMethod")));
    }

    @Test
    public void testCompleteCheckoutFlow() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);

        // Step 1: Initiate checkout
        Response checkoutResponse = ApiUtils.performPostRequest(requestSpec, "/api/checkout", "{}");
        assertThat(checkoutResponse.getStatusCode()).isEqualTo(200);
        String checkoutId = checkoutResponse.jsonPath().getString("checkoutId");

        // Step 2: Validate shipping address
        String addressRequestBody = "{\n" +
                "  \"address\": {\n" +
                "    \"street\": \"123 Main St\",\n" +
                "    \"city\": \"New York\",\n" +
                "    \"state\": \"NY\",\n" +
                "    \"zipCode\": \"10001\",\n" +
                "    \"country\": \"USA\"\n" +
                "  }\n" +
                "}";
        Response addressResponse = ApiUtils.performPostRequest(requestSpec, "/api/checkout/shipping-address",
                addressRequestBody);
        assertThat(addressResponse.getStatusCode()).isEqualTo(200);
        assertThat(addressResponse.jsonPath().getBoolean("valid")).isTrue();

        // Step 3: Select shipping method
        String shippingMethodRequestBody = "{\n" +
                "  \"shippingMethodId\": \"express\"\n" +
                "}";
        Response shippingResponse = ApiUtils.performPostRequest(requestSpec, "/api/checkout/shipping-method",
                shippingMethodRequestBody);
        assertThat(shippingResponse.getStatusCode()).isEqualTo(200);

        // Step 4: Process payment
        String paymentRequestBody = "{\n" +
                "  \"paymentMethod\": \"Credit Card\",\n" +
                "  \"cardNumber\": \"4111111111111111\",\n" +
                "  \"expiryMonth\": \"12\",\n" +
                "  \"expiryYear\": \"2025\",\n" +
                "  \"cvv\": \"123\"\n" +
                "}";
        Response paymentResponse = ApiUtils.performPostRequest(requestSpec, "/api/checkout/payment",
                paymentRequestBody);
        assertThat(paymentResponse.getStatusCode()).isEqualTo(200);
        String newOrderId = paymentResponse.jsonPath().getString("orderId");

        // Step 5: Get the created order
        Response orderResponse = ApiUtils.performGetRequest(requestSpec, "/api/orders/" + newOrderId);
        assertThat(orderResponse.getStatusCode()).isEqualTo(200);

        // Verify order details
        JsonPath orderJson = orderResponse.jsonPath();
        assertThat(orderJson.getString("id")).isEqualTo(orderId);
        assertThat(orderJson.getString("status")).isEqualTo("pending");

        // Verify payment info
        assertThat(orderJson.getString("paymentInfo.method")).isEqualTo("Credit Card");
        assertThat(orderJson.getString("paymentInfo.status")).isEqualTo("completed");

        // Verify shipping address
        assertThat(orderJson.getString("shippingAddress.street")).isEqualTo("123 Main Street");

        // Verify the complete flow was executed
        verify(postRequestedFor(urlPathEqualTo("/api/checkout")));
        verify(postRequestedFor(urlPathEqualTo("/api/checkout/shipping-address")));
        verify(postRequestedFor(urlPathEqualTo("/api/checkout/shipping-method")));
        verify(postRequestedFor(urlPathEqualTo("/api/checkout/payment")));
        verify(getRequestedFor(urlPathEqualTo("/api/orders/" + orderId)));
    }
}
