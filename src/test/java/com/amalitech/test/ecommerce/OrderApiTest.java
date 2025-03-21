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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderApiTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(OrderApiTest.class);
    private WireMockServer wireMockServer;
    private String authToken = "Bearer mock-jwt-token";
    private String orderId;

    @BeforeClass
    public void setUp() {
        super.setupClass();
        wireMockServer = getWireMockServer();
        orderId = UUID.randomUUID().toString();
        setupOrderStubs();
    }

    private void setupOrderStubs() {
        if (wireMockServer == null)
            return;

        // Stub for getting all orders
        wireMockServer.stubFor(get(urlPathEqualTo("/api/orders"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"orders\": [\n" +
                                "    {\n" +
                                "      \"id\": \"" + orderId + "\",\n" +
                                "      \"createdAt\": \"2023-07-15T08:30:45Z\",\n" +
                                "      \"status\": \"delivered\",\n" +
                                "      \"total\": 684.99,\n" +
                                "      \"items\": 2\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"id\": \"order-456\",\n" +
                                "      \"createdAt\": \"2023-06-30T14:22:33Z\",\n" +
                                "      \"status\": \"processing\",\n" +
                                "      \"total\": 124.95,\n" +
                                "      \"items\": 1\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"totalCount\": 2,\n" +
                                "  \"page\": 1,\n" +
                                "  \"pageSize\": 10,\n" +
                                "  \"totalPages\": 1\n" +
                                "}")));

        // Stub for filtering orders by status
        wireMockServer.stubFor(get(urlPathEqualTo("/api/orders"))
                .withHeader("Authorization", matching("Bearer .*"))
                .withQueryParam("status", equalTo("processing"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"orders\": [\n" +
                                "    {\n" +
                                "      \"id\": \"order-456\",\n" +
                                "      \"createdAt\": \"2023-06-30T14:22:33Z\",\n" +
                                "      \"status\": \"processing\",\n" +
                                "      \"total\": 124.95,\n" +
                                "      \"items\": 1\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"totalCount\": 1,\n" +
                                "  \"page\": 1,\n" +
                                "  \"pageSize\": 10,\n" +
                                "  \"totalPages\": 1\n" +
                                "}")));

        // Stub for filtering orders by date range
        wireMockServer.stubFor(get(urlPathEqualTo("/api/orders"))
                .withHeader("Authorization", matching("Bearer .*"))
                .withQueryParam("fromDate", matching("\\d{4}-\\d{2}-\\d{2}"))
                .withQueryParam("toDate", matching("\\d{4}-\\d{2}-\\d{2}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"orders\": [\n" +
                                "    {\n" +
                                "      \"id\": \"" + orderId + "\",\n" +
                                "      \"createdAt\": \"2023-07-15T08:30:45Z\",\n" +
                                "      \"status\": \"delivered\",\n" +
                                "      \"total\": 684.99,\n" +
                                "      \"items\": 2\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"totalCount\": 1,\n" +
                                "  \"page\": 1,\n" +
                                "  \"pageSize\": 10,\n" +
                                "  \"totalPages\": 1\n" +
                                "}")));

        // Stub for getting a single order
        wireMockServer.stubFor(get(urlPathMatching("/api/orders/" + orderId))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"id\": \"" + orderId + "\",\n" +
                                "  \"userId\": \"user-123\",\n" +
                                "  \"status\": \"delivered\",\n" +
                                "  \"createdAt\": \"2023-07-15T08:30:45Z\",\n" +
                                "  \"items\": [\n" +
                                "    {\n" +
                                "      \"productId\": 1,\n" +
                                "      \"productName\": \"Smartphone\",\n" +
                                "      \"quantity\": 1,\n" +
                                "      \"unitPrice\": 599.99,\n" +
                                "      \"totalPrice\": 599.99\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"productId\": 3,\n" +
                                "      \"productName\": \"Phone Case\",\n" +
                                "      \"quantity\": 1,\n" +
                                "      \"unitPrice\": 24.99,\n" +
                                "      \"totalPrice\": 24.99\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"subtotal\": 624.98,\n" +
                                "  \"tax\": 60.00,\n" +
                                "  \"shipping\": 0.00,\n" +
                                "  \"total\": 684.98,\n" +
                                "  \"shippingAddress\": {\n" +
                                "    \"street\": \"123 Main Street\",\n" +
                                "    \"city\": \"New York\",\n" +
                                "    \"state\": \"NY\",\n" +
                                "    \"zipCode\": \"10001\",\n" +
                                "    \"country\": \"USA\"\n" +
                                "  },\n" +
                                "  \"paymentInfo\": {\n" +
                                "    \"method\": \"Credit Card\",\n" +
                                "    \"transactionId\": \"txn-abc-xyz\",\n" +
                                "    \"status\": \"completed\"\n" +
                                "  },\n" +
                                "  \"trackingNumber\": \"TRK12345678\",\n" +
                                "  \"deliveryDate\": \"2023-07-18T14:30:00Z\"\n" +
                                "}")));

        // Stub for order not found
        wireMockServer.stubFor(get(urlPathEqualTo("/api/orders/invalid-id"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"error\": \"Order not found\"\n" +
                                "}")));

        // Stub for cancelling an order
        wireMockServer.stubFor(post(urlPathMatching("/api/orders/" + orderId + "/cancel"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"id\": \"" + orderId + "\",\n" +
                                "  \"status\": \"cancelled\",\n" +
                                "  \"cancelledAt\": \"2023-07-16T10:15:30Z\",\n" +
                                "  \"refundStatus\": \"pending\"\n" +
                                "}")));

        // Stub for order tracking
        wireMockServer.stubFor(get(urlPathMatching("/api/orders/" + orderId + "/tracking"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"orderId\": \"" + orderId + "\",\n" +
                                "  \"trackingNumber\": \"TRK12345678\",\n" +
                                "  \"carrier\": \"FedEx\",\n" +
                                "  \"status\": \"Delivered\",\n" +
                                "  \"estimatedDelivery\": \"2023-07-18T00:00:00Z\",\n" +
                                "  \"actualDelivery\": \"2023-07-18T14:30:00Z\",\n" +
                                "  \"events\": [\n" +
                                "    {\n" +
                                "      \"timestamp\": \"2023-07-15T10:00:00Z\",\n" +
                                "      \"description\": \"Order shipped\",\n" +
                                "      \"location\": \"Warehouse A\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"timestamp\": \"2023-07-17T08:30:00Z\",\n" +
                                "      \"description\": \"Out for delivery\",\n" +
                                "      \"location\": \"Local Distribution Center\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"timestamp\": \"2023-07-18T14:30:00Z\",\n" +
                                "      \"description\": \"Delivered\",\n" +
                                "      \"location\": \"Customer Address\"\n" +
                                "    }\n" +
                                "  ]\n" +
                                "}")));

        // Stub for requesting return
        wireMockServer.stubFor(post(urlPathMatching("/api/orders/" + orderId + "/return"))
                .withHeader("Authorization", matching("Bearer .*"))
                .withRequestBody(matchingJsonPath("$.reason"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"returnId\": \"return-789\",\n" +
                                "  \"orderId\": \"" + orderId + "\",\n" +
                                "  \"status\": \"pending_approval\",\n" +
                                "  \"createdAt\": \"2023-07-20T09:45:12Z\",\n" +
                                "  \"items\": [\n" +
                                "    {\n" +
                                "      \"productId\": 1,\n" +
                                "      \"quantity\": 1\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"reason\": \"Defective product\",\n" +
                                "  \"returnLabel\": \"https://example.com/return-labels/return-789.pdf\"\n" +
                                "}")));
    }

    @Test
    public void testGetAllOrders() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/orders");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> orders = jsonPath.getList("orders");

        // Verify orders list
        assertThat(orders).hasSize(2);
        assertThat(jsonPath.getInt("totalCount")).isEqualTo(2);

        // Check first order
        assertThat(jsonPath.getString("orders[0].id")).isEqualTo(orderId);
        assertThat(jsonPath.getString("orders[0].status")).isEqualTo("delivered");

        // Verify pagination info
        assertThat(jsonPath.getInt("page")).isEqualTo(1);
        assertThat(jsonPath.getInt("pageSize")).isEqualTo(10);
        assertThat(jsonPath.getInt("totalPages")).isEqualTo(1);

        // Verify the request was made
        verify(getRequestedFor(urlPathEqualTo("/api/orders"))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testFilterOrdersByStatus() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);
        String status = "processing";

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/orders?status=" + status);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> orders = jsonPath.getList("orders");

        // Verify filtered orders
        assertThat(orders).hasSize(1);
        assertThat(jsonPath.getInt("totalCount")).isEqualTo(1);
        assertThat(jsonPath.getString("orders[0].status")).isEqualTo(status);

        // Verify the request was made
        verify(getRequestedFor(urlPathEqualTo("/api/orders"))
                .withHeader("Authorization", equalTo(authToken))
                .withQueryParam("status", equalTo(status)));
    }

    @Test
    public void testFilterOrdersByDateRange() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);
        String fromDate = "2023-07-01";
        String toDate = "2023-07-31";

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec,
                "/api/orders?fromDate=" + fromDate + "&toDate=" + toDate);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> orders = jsonPath.getList("orders");

        // Verify filtered orders
        assertThat(orders).hasSize(1);
        assertThat(jsonPath.getString("orders[0].id")).isEqualTo(orderId);

        // Verify the request was made
        verify(getRequestedFor(urlPathEqualTo("/api/orders"))
                .withHeader("Authorization", equalTo(authToken))
                .withQueryParam("fromDate", equalTo(fromDate))
                .withQueryParam("toDate", equalTo(toDate)));
    }

    @Test
    public void testGetOrderDetails() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/orders/" + orderId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();

        // Check order details
        assertThat(jsonPath.getString("id")).isEqualTo(orderId);
        assertThat(jsonPath.getString("status")).isEqualTo("delivered");

        // Check items
        List<Map<String, Object>> items = jsonPath.getList("items");
        assertThat(items).hasSize(2);
        assertThat(jsonPath.getString("items[0].productName")).isEqualTo("Smartphone");

        // Check financial information
        assertThat(jsonPath.getDouble("subtotal")).isEqualTo(624.98);
        assertThat(jsonPath.getDouble("tax")).isEqualTo(60.00);
        assertThat(jsonPath.getDouble("total")).isEqualTo(684.98);

        // Check shipping info
        assertThat(jsonPath.getString("shippingAddress.city")).isEqualTo("New York");
        assertThat(jsonPath.getString("trackingNumber")).isEqualTo("TRK12345678");

        // Verify the request was made
        verify(getRequestedFor(urlPathMatching("/api/orders/" + orderId))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testOrderNotFound() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/orders/invalid-id");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(404);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("error")).isEqualTo("Order not found");

        // Verify the request was made
        verify(getRequestedFor(urlPathEqualTo("/api/orders/invalid-id"))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testCancelOrder() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/orders/" + orderId + "/cancel", "{}");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("id")).isEqualTo(orderId);
        assertThat(jsonPath.getString("status")).isEqualTo("cancelled");
        assertThat(jsonPath.getString("cancelledAt")).isNotEmpty();
        assertThat(jsonPath.getString("refundStatus")).isEqualTo("pending");

        // Verify the request was made
        verify(postRequestedFor(urlPathMatching("/api/orders/" + orderId + "/cancel"))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testOrderTracking() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/orders/" + orderId + "/tracking");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("orderId")).isEqualTo(orderId);
        assertThat(jsonPath.getString("trackingNumber")).isEqualTo("TRK12345678");
        assertThat(jsonPath.getString("status")).isEqualTo("Delivered");

        // Check tracking events
        List<Map<String, Object>> events = jsonPath.getList("events");
        assertThat(events).hasSize(3);

        // Check the delivery event (most recent)
        Map<String, Object> deliveryEvent = events.get(2);
        assertThat(deliveryEvent.get("description")).isEqualTo("Delivered");
        assertThat(deliveryEvent.get("location")).isEqualTo("Customer Address");

        // Verify the request was made
        verify(getRequestedFor(urlPathMatching("/api/orders/" + orderId + "/tracking"))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testRequestReturn() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        requestSpec.header("Authorization", authToken);
        String requestBody = "{\n" +
                "  \"reason\": \"Defective product\",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"productId\": 1,\n" +
                "      \"quantity\": 1\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/orders/" + orderId + "/return", requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("returnId")).isNotEmpty();
        assertThat(jsonPath.getString("orderId")).isEqualTo(orderId);
        assertThat(jsonPath.getString("status")).isEqualTo("pending_approval");
        assertThat(jsonPath.getString("reason")).isEqualTo("Defective product");
        assertThat(jsonPath.getString("returnLabel")).startsWith("https://");

        // Verify the request was made
        verify(postRequestedFor(urlPathMatching("/api/orders/" + orderId + "/return"))
                .withHeader("Authorization", equalTo(authToken))
                .withRequestBody(matchingJsonPath("$.reason")));
    }
}
