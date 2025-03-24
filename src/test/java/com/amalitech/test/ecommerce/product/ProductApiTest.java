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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductApiTest extends BaseTest {
    private WireMockServer wireMockServer;
    private static final String PRODUCTS_BASE_PATH = "/api/products";

    @BeforeClass
    public void setUp() {
        super.setupClass();
         wireMockServer = getWireMockServer();
//        log.info("WireMock server setup with mappings from resources directory. WireMock server is {}null",
//                wireMockServer == null ? "" : "not ");

        // Comment out or remove this line to use the mock server

    }

    @Test
    public void testGetAllProducts() {

        Response response = ApiUtils.performGetRequest(requestSpec, PRODUCTS_BASE_PATH);

        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> products = jsonPath.getList("products");

        assertThat(products)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThanOrEqualTo(2);

        Map<String, Object> firstProduct = products.getFirst();
        assertThat(firstProduct).containsKeys("id", "name", "price");

        verify(getRequestedFor(urlEqualTo(PRODUCTS_BASE_PATH)));
    }

    @Test
    public void testSearchProducts() {

        String searchQuery = "phone";
        Response response = ApiUtils.performGetRequest(
                requestSpec,
                PRODUCTS_BASE_PATH + "/search?query=" + searchQuery
        );

        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> results = jsonPath.getList("results");

        assertThat(results)
                .isNotNull()
                .isNotEmpty();

        verify(getRequestedFor(urlPathEqualTo(PRODUCTS_BASE_PATH + "/search"))
                .withQueryParam("query", equalTo(searchQuery)));
    }

    @Test
    public void testGetProductDetails() {

        long productId = 1L;
        Response response = ApiUtils.performGetRequest(
                requestSpec,
                PRODUCTS_BASE_PATH + "/" + productId
        );

        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        Map<String, Object> product = jsonPath.getMap("$");

        assertThat(product)
                .isNotNull()
                .containsKeys("id", "name", "price", "category");

        verify(getRequestedFor(urlEqualTo(PRODUCTS_BASE_PATH + "/" + productId)));
    }

//    @Test
//    public void testProductNotFound() {
//
//        int nonExistentId = 999;
//        Response response = ApiUtils.performGetRequest(
//                requestSpec,
//                PRODUCTS_BASE_PATH + "/" + nonExistentId
//        );
//
//        assertThat(response.getStatusCode()).isEqualTo(404);
//
//        JsonPath jsonPath = response.jsonPath();
//        String error = jsonPath.getString("error");
//
//        assertThat(error).isEqualTo("Product not found");
//
//        verify(getRequestedFor(urlEqualTo(PRODUCTS_BASE_PATH + "/" + nonExistentId)));
//    }

    @Test
    public void testGetProductCategories() {

        Response response = ApiUtils.performGetRequest(requestSpec, PRODUCTS_BASE_PATH + "/categories");

        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<String> categories = jsonPath.getList("categories");

        assertThat(categories)
                .isNotNull()
                .containsExactly("Electronics", "Clothing", "Books", "Home & Kitchen");

        verify(getRequestedFor(urlEqualTo(PRODUCTS_BASE_PATH + "/categories")));
    }

    @Test
    public void testGetProductReviews() {


        long productId = 1L;
        Response response = ApiUtils.performGetRequest(
                requestSpec,
                PRODUCTS_BASE_PATH + "/" + productId + "/reviews"
        );

        assertThat(response.getStatusCode()).isEqualTo(200);

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> reviews = jsonPath.getList("$");

        assertThat(reviews)
                .isNotNull()
                .isNotEmpty();

        if (!reviews.isEmpty()) {
            Map<String, Object> firstReview = reviews.getFirst();
            assertThat(firstReview).containsKeys("id", "rating", "comment", "productId");
        }

        verify(getRequestedFor(urlEqualTo(PRODUCTS_BASE_PATH + "/" + productId + "/reviews")));
    }

}