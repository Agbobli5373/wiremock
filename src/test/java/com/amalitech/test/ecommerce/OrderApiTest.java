package com.amalitech.test.ecommerce;

import com.amalitech.test.base.BaseTest;
import com.amalitech.test.utils.ApiUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderApiTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(OrderApiTest.class);
    private WireMockServer wireMockServer;
    private static final String ORDERS_BASE_PATH = "/api/orders";
    private final String authToken = "Bearer mock-jwt-token";
    private String orderId;

    @BeforeClass
    public void setUp() {
        super.setupClass();
        orderId = UUID.randomUUID().toString();

        // Comment out or remove this line to use the mock server
        useRealServer("https://localhost:8080");
    }

    @Test
    public void testGetAllOrders() {
        if (shouldSkipMockTest()) return;

        // Arrange
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, ORDERS_BASE_PATH);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> orders = jsonPath.getList("orders");

        assertThat(orders)
                .isNotNull()
                .hasSize(2);
        assertThat(jsonPath.getInt("totalCount")).isEqualTo(2);
        assertThat(jsonPath.getString("orders[0].id")).isEqualTo(orderId);
        assertThat(jsonPath.getString("orders[0].status")).isEqualTo("delivered");

        // Verify pagination info
        assertThat(jsonPath.getInt("page")).isEqualTo(1);
        assertThat(jsonPath.getInt("pageSize")).isEqualTo(10);
        assertThat(jsonPath.getInt("totalPages")).isEqualTo(1);

        verify(getRequestedFor(urlPathEqualTo(ORDERS_BASE_PATH))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testFilterOrdersByStatus() {
        if (shouldSkipMockTest()) return;

        // Arrange
        requestSpec.header("Authorization", authToken);
        String status = "processing";

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec,
                ORDERS_BASE_PATH + "?status=" + status);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> orders = jsonPath.getList("orders");

        assertThat(orders).hasSize(1);
        assertThat(jsonPath.getInt("totalCount")).isEqualTo(1);
        assertThat(jsonPath.getString("orders[0].status")).isEqualTo(status);

        verify(getRequestedFor(urlPathEqualTo(ORDERS_BASE_PATH))
                .withHeader("Authorization", equalTo(authToken))
                .withQueryParam("status", equalTo(status)));
    }

    @Test
    public void testFilterOrdersByDateRange() {
        if (shouldSkipMockTest()) return;

        // Arrange
        requestSpec.header("Authorization", authToken);
        String fromDate = "2023-07-01";
        String toDate = "2023-07-31";

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec,
                ORDERS_BASE_PATH + "?fromDate=" + fromDate + "&toDate=" + toDate);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> orders = jsonPath.getList("orders");

        assertThat(orders).hasSize(1);
        assertThat(jsonPath.getString("orders[0].id")).isEqualTo(orderId);

        verify(getRequestedFor(urlPathEqualTo(ORDERS_BASE_PATH))
                .withHeader("Authorization", equalTo(authToken))
                .withQueryParam("fromDate", equalTo(fromDate))
                .withQueryParam("toDate", equalTo(toDate)));
    }

    @Test
    public void testGetOrderDetails() {
        if (shouldSkipMockTest()) return;

        // Arrange
        requestSpec.header("Authorization", authToken);
        String orderPath = ORDERS_BASE_PATH + "/" + orderId;

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, orderPath);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();

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

        verify(getRequestedFor(urlPathMatching(orderPath))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testOrderNotFound() {
        if (shouldSkipMockTest()) return;

        // Arrange
        requestSpec.header("Authorization", authToken);
        String invalidOrderPath = ORDERS_BASE_PATH + "/invalid-id";

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, invalidOrderPath);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("error")).isEqualTo("Order not found");

        verify(getRequestedFor(urlPathEqualTo(invalidOrderPath))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testCancelOrder() {
        if (shouldSkipMockTest()) return;

        // Arrange
        requestSpec.header("Authorization", authToken);
        String cancelPath = ORDERS_BASE_PATH + "/" + orderId + "/cancel";

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, cancelPath, "{}");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("id")).isEqualTo(orderId);
        assertThat(jsonPath.getString("status")).isEqualTo("cancelled");
        assertThat(jsonPath.getString("cancelledAt")).isNotEmpty();
        assertThat(jsonPath.getString("refundStatus")).isEqualTo("pending");

        verify(postRequestedFor(urlPathMatching(cancelPath))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testOrderTracking() {
        if (shouldSkipMockTest()) return;

        // Arrange
        requestSpec.header("Authorization", authToken);
        String trackingPath = ORDERS_BASE_PATH + "/" + orderId + "/tracking";

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, trackingPath);

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

        verify(getRequestedFor(urlPathMatching(trackingPath))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testRequestReturn() {
        if (shouldSkipMockTest()) return;

        // Arrange
        requestSpec.header("Authorization", authToken);
        String returnPath = ORDERS_BASE_PATH + "/" + orderId + "/return";
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
        Response response = ApiUtils.performPostRequest(requestSpec, returnPath, requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("returnId")).isNotEmpty();
        assertThat(jsonPath.getString("orderId")).isEqualTo(orderId);
        assertThat(jsonPath.getString("status")).isEqualTo("pending_approval");
        assertThat(jsonPath.getString("reason")).isEqualTo("Defective product");
        assertThat(jsonPath.getString("returnLabel")).startsWith("https://");

        verify(postRequestedFor(urlPathMatching(returnPath))
                .withHeader("Authorization", equalTo(authToken))
                .withRequestBody(matchingJsonPath("$.reason")));
    }

    private boolean shouldSkipMockTest() {
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return false;
        }
        return false;
    }
}