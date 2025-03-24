package com.amalitech.test.ecommerce.auth;

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
    private static final String JSON_BASE_PATH = "json/auth/";
    private static final String AUTH_TOKEN = "Bearer mock-jwt-token";

    private static final String LOGIN_REQUEST_FALLBACK = """
            {
              "email": "user@example.com",
              "password": "securepassword"
            }""";

    private static final String LOGIN_INVALID_REQUEST_FALLBACK = """
            {
              "email": "invalid@example.com",
              "password": "wrongpassword"
            }""";

    @BeforeClass
    public void setUp() {
        super.setupClass();
        //comment out or remove this line to use the mock server
        WireMockServer wireMockServer = getWireMockServer();
        log.info("WireMock server setup with mappings from resources directory. WireMock server is {}null",
                wireMockServer == null ? "" : "not ");

        // Comment out or remove this line to use the mock server
//         useRealServer("https://localhost:8080");
    }

    @Test
    public void testSuccessfulLogin() {

        log.info("Running testSuccessfulLogin");

        String requestBody = JsonUtils.getJsonContentOrFallback(
                JSON_BASE_PATH + "login-request.json",
                LOGIN_REQUEST_FALLBACK);

        requestSpec.contentType("application/json");

        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/login", requestBody);

        assertThat(response.getStatusCode()).isEqualTo(200);

        User user = response.as(User.class);
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getToken()).isNotEmpty();

        verify(postRequestedFor(urlPathEqualTo("/api/auth/login"))
                .withRequestBody(matchingJsonPath("$.email"))
                .withRequestBody(matchingJsonPath("$.password")));
    }

    @Test
    public void testInvalidLogin() {

        String requestBody = JsonUtils.getJsonContentOrFallback(
                JSON_BASE_PATH + "login-invalid-request.json",
                LOGIN_INVALID_REQUEST_FALLBACK);

        requestSpec.contentType("application/json");

        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/login/wrong", requestBody);

        assertThat(response.getStatusCode()).isEqualTo(401);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("error")).isEqualTo("Invalid email or password");

        verify(postRequestedFor(urlPathEqualTo("/api/auth/login/wrong"))
                .withRequestBody(matchingJsonPath("$.email", equalTo("invalid@example.com"))));
    }

    @Test
    public void testSuccessfulRegistration() {

        String requestBody = JsonUtils.loadJsonFromResources(JSON_BASE_PATH + "register-request.json");
        requestSpec.contentType("application/json");

        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/register", requestBody);

        assertThat(response.getStatusCode()).isEqualTo(201);

        User user = response.as(User.class);
        assertThat(user.getEmail()).isEqualTo("newuser@example.com");
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getToken()).isNotEmpty();

        verify(postRequestedFor(urlPathEqualTo("/api/auth/register"))
                .withRequestBody(matchingJsonPath("$.email"))
                .withRequestBody(matchingJsonPath("$.password")));
    }

    @Test
    public void testRegistrationWithExistingEmail() {

        String requestBody = JsonUtils.loadJsonFromResources(JSON_BASE_PATH + "register-existing-request.json");
        requestSpec.contentType("application/json");

        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/register/exited", requestBody);

        assertThat(response.getStatusCode()).isEqualTo(400);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("error")).isEqualTo("Email already exists");

        verify(postRequestedFor(urlPathEqualTo("/api/auth/register/exited"))
                .withRequestBody(matchingJsonPath("$.email", equalTo("existing@example.com"))));
    }

    @Test
    public void testGetProfile() {

        requestSpec.header("Authorization", AUTH_TOKEN);

        Response response = ApiUtils.performGetRequest(requestSpec, "/api/auth/profile");

        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getString("email")).isEqualTo("user@example.com");
        assertThat(jsonPath.getString("firstName")).isEqualTo("John");
        assertThat(jsonPath.getString("lastName")).isEqualTo("Doe");

        assertThat(jsonPath.getList("shippingAddresses")).hasSize(1);
        assertThat(jsonPath.getString("shippingAddresses[0].street")).isEqualTo("123 Main St");
        assertThat(jsonPath.getBoolean("shippingAddresses[0].isDefault")).isTrue();

        verify(getRequestedFor(urlPathEqualTo("/api/auth/profile"))
                .withHeader("Authorization", equalTo(AUTH_TOKEN)));
    }

    @Test
    public void testChangePassword() {

        requestSpec.header("Authorization", AUTH_TOKEN)
                .contentType("application/json");

        String requestBody = JsonUtils.loadJsonFromResources(JSON_BASE_PATH + "change-password-request.json");

        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/change-password", requestBody);

        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getBoolean("success")).isTrue();
        assertThat(jsonPath.getString("message")).isEqualTo("Password changed successfully");

        verify(postRequestedFor(urlPathEqualTo("/api/auth/change-password"))
                .withHeader("Authorization", equalTo(AUTH_TOKEN))
                .withRequestBody(matchingJsonPath("$.currentPassword"))
                .withRequestBody(matchingJsonPath("$.newPassword")));
    }

    @Test
    public void testLogout() {

        requestSpec.header("Authorization", AUTH_TOKEN);

        Response response = ApiUtils.performPostRequest(requestSpec, "/api/auth/logout", "");

        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        assertThat(jsonPath.getBoolean("success")).isTrue();
        assertThat(jsonPath.getString("message")).isEqualTo("Logged out successfully");

        verify(postRequestedFor(urlPathEqualTo("/api/auth/logout"))
                .withHeader("Authorization", equalTo(AUTH_TOKEN)));
    }

}