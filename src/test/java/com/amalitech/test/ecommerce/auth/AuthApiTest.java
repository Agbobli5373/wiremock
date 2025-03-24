package com.amalitech.test.ecommerce;

import com.amalitech.test.base.BaseTest;
import com.amalitech.test.model.User;
import com.amalitech.test.utils.ApiUtils;
import com.amalitech.test.utils.JsonUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthApiTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(AuthApiTest.class);
    private WireMockServer wireMockServer;
    private static final String JSON_BASE_PATH = "json/auth/";

    // Fallback JSON strings
    private static final String LOGIN_REQUEST_FALLBACK = "{\n" +
            "  \"email\": \"user@example.com\",\n" +
            "  \"password\": \"securepassword\"\n" +
            "}";

    private static final String LOGIN_INVALID_REQUEST_FALLBACK = "{\n" +
            "  \"email\": \"invalid@example.com\",\n" +
            "  \"password\": \"wrongpassword\"\n" +
            "}";

    private static final String REGISTER_REQUEST_FALLBACK = "{\n" +
            "  \"email\": \"newuser@example.com\",\n" +
            "  \"password\": \"securepassword\",\n" +
            "  \"firstName\": \"Jane\",\n" +
            "  \"lastName\": \"Smith\"\n" +
            "}";

    private static final String REGISTER_EXISTING_REQUEST_FALLBACK = "{\n" +
            "  \"email\": \"existing@example.com\",\n" +
            "  \"password\": \"securepassword\",\n" +
            "  \"firstName\": \"Existing\",\n" +
            "  \"lastName\": \"User\"\n" +
            "}";

    private static final String CHANGE_PASSWORD_REQUEST_FALLBACK = "{\n" +
            "  \"currentPassword\": \"oldpassword\",\n" +
            "  \"newPassword\": \"newpassword123\"\n" +
            "}";

    @BeforeClass
    public void setUp() {
        super.setupClass();
        wireMockServer = getWireMockServer();
        log.info("WireMock server setup with mappings from resources directory. WireMock server is {}null",
                wireMockServer == null ? "" : "not ");
    }

    @Test
    public void testSuccessfulLogin() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        log.info("Running testSuccessfulLogin");

        // Arrange
        String requestBody = JsonUtils.getJsonContentOrFallback(
                JSON_BASE_PATH + "login-request.json",
                LOGIN_REQUEST_FALLBACK);

        // Set the proper Content-Type header for JSON
        requestSpec.contentType("application/json");

        log.debug("Login request body: {}", requestBody);

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/login", requestBody);
        log.debug("Response status code: {}", response.getStatusCode());
        log.debug("Response body: {}", response.asString());

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        User user = response.as(User.class);
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getToken()).isNotEmpty();

        // Verify the request was made using the WireMock Admin API
        verify(postRequestedFor(urlPathEqualTo("/api/auth/login"))
                .withRequestBody(matchingJsonPath("$.email"))
                .withRequestBody(matchingJsonPath("$.password")));
    }

    // ... other test methods remain the same, but with
    // contentType("application/json") added to requestSpec ...

    @Test
    public void testInvalidLogin() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        log.info("Running testInvalidLogin");

        // Arrange
        String requestBody = JsonUtils.getJsonContentOrFallback(
                JSON_BASE_PATH + "login-invalid-request.json",
                LOGIN_INVALID_REQUEST_FALLBACK);

        // Set the proper Content-Type header for JSON
        requestSpec.contentType("application/json");

        log.debug("Invalid login request body: {}", requestBody);

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/login", requestBody);
        log.debug("Response status code: {}", response.getStatusCode());
        log.debug("Response body: {}", response.asString());

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(401);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("error")).isEqualTo("Invalid email or password");

        // Verify the request was made
        verify(postRequestedFor(urlPathEqualTo("/api/auth/login"))
                .withRequestBody(matchingJsonPath("$.email", equalTo("invalid@example.com"))));
    }

    // ... remaining test methods with contentType("application/json") added ...

    @Test
    public void testSuccessfulRegistration() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        String requestBody = JsonUtils.loadJsonFromResources(JSON_BASE_PATH + "register-request.json");

        // Set the proper Content-Type header for JSON
        requestSpec.contentType("application/json");

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/register", requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(201);

        User user = response.as(User.class);
        assertThat(user.getEmail()).isEqualTo("newuser@example.com");
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getToken()).isNotEmpty();

        // Verify the request was made
        verify(postRequestedFor(urlPathEqualTo("/api/auth/register"))
                .withRequestBody(matchingJsonPath("$.email"))
                .withRequestBody(matchingJsonPath("$.password")));
    }

    @Test
    public void testRegistrationWithExistingEmail() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        String requestBody = JsonUtils.loadJsonFromResources(JSON_BASE_PATH + "register-existing-request.json");

        // Set the proper Content-Type header for JSON
        requestSpec.contentType("application/json");

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/register", requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(400);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("error")).isEqualTo("Email already exists");

        // Verify the request was made
        verify(postRequestedFor(urlPathEqualTo("/api/auth/register"))
                .withRequestBody(matchingJsonPath("$.email", equalTo("existing@example.com"))));
    }

    @Test
    public void testGetProfile() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        String authToken = "Bearer mock-jwt-token";
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performGetRequest(requestSpec, "/api/auth/profile");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("email")).isEqualTo("user@example.com");
        assertThat(jsonPath.getString("firstName")).isEqualTo("John");
        assertThat(jsonPath.getString("lastName")).isEqualTo("Doe");

        // Check shipping addresses
        assertThat(jsonPath.getList("shippingAddresses")).hasSize(1);
        assertThat(jsonPath.getString("shippingAddresses[0].street")).isEqualTo("123 Main St");
        assertThat(jsonPath.getBoolean("shippingAddresses[0].isDefault")).isTrue();

        // Verify the request was made
        verify(getRequestedFor(urlPathEqualTo("/api/auth/profile"))
                .withHeader("Authorization", equalTo(authToken)));
    }

    @Test
    public void testChangePassword() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        String authToken = "Bearer mock-jwt-token";
        requestSpec.header("Authorization", authToken);

        String requestBody = JsonUtils.loadJsonFromResources(JSON_BASE_PATH + "change-password-request.json");

        // Set the proper Content-Type header for JSON
        requestSpec.contentType("application/json");

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/change-password", requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getBoolean("success")).isTrue();
        assertThat(jsonPath.getString("message")).isEqualTo("Password changed successfully");

        // Verify the request was made
        verify(postRequestedFor(urlPathEqualTo("/api/auth/change-password"))
                .withHeader("Authorization", equalTo(authToken))
                .withRequestBody(matchingJsonPath("$.currentPassword"))
                .withRequestBody(matchingJsonPath("$.newPassword")));
    }

    @Test
    public void testLogout() {
        // Skip test if not using mock server
        if (wireMockServer == null) {
            log.info("Skipping mock-based test as we're using a real server");
            return;
        }

        // Arrange
        String authToken = "Bearer mock-jwt-token";
        requestSpec.header("Authorization", authToken);

        // Act
        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/logout", "");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getBoolean("success")).isTrue();
        assertThat(jsonPath.getString("message")).isEqualTo("Logged out successfully");

        // Verify the request was made
        verify(postRequestedFor(urlPathEqualTo("/api/auth/logout"))
                .withHeader("Authorization", equalTo(authToken)));
    }
}
