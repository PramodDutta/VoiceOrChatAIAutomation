package com.voiceai.automation.tests;

import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static org.testng.Assert.*;

/**
 * Sample API Test class demonstrating REST Assured with TestNG and Allure integration.
 * Uses JSONPlaceholder API (https://jsonplaceholder.typicode.com) for demonstration.
 */
@Epic("API Testing")
@Feature("JSONPlaceholder API")
public class SampleApiTest {

    private static final Logger logger = LogManager.getLogger(SampleApiTest.class);
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    @BeforeClass
    @Step("Setup REST Assured configuration")
    public void setup() {
        logger.info("Setting up REST Assured configuration");
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        RestAssured.filters(new io.qameta.allure.restassured.AllureRestAssured());
        logger.info("Base URI set to: {}", BASE_URL);
    }

    @Test(groups = {"smoke", "api"})
    @Story("GET Requests")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that GET /posts returns a list of posts with status code 200")
    public void testGetAllPosts() {
        logger.info("Starting test: testGetAllPosts");

        Response response = given()
                .contentType(ContentType.JSON)
            .when()
                .get("/posts")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        int postCount = response.jsonPath().getList("$").size();
        logger.info("Retrieved {} posts", postCount);
        assertTrue(postCount > 0, "Expected at least one post to be returned");
    }

    @Test(groups = {"smoke", "api"})
    @Story("GET Requests")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that GET /posts/{id} returns a specific post")
    public void testGetSinglePost() {
        int postId = 1;
        logger.info("Starting test: testGetSinglePost with id={}", postId);

        Response response = given()
                .contentType(ContentType.JSON)
                .pathParam("id", postId)
            .when()
                .get("/posts/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        int returnedId = response.jsonPath().getInt("id");
        String title = response.jsonPath().getString("title");
        
        logger.info("Retrieved post with id={}, title={}", returnedId, title);
        assertEquals(returnedId, postId, "Post ID should match the requested ID");
        assertNotNull(title, "Post title should not be null");
    }

    @Test(groups = {"api", "regression"})
    @Story("POST Requests")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that POST /posts creates a new post")
    public void testCreatePost() {
        logger.info("Starting test: testCreatePost");

        String requestBody = """
            {
                "title": "Test Post Title",
                "body": "This is a test post body created by automation",
                "userId": 1
            }
            """;

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post("/posts")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        int newPostId = response.jsonPath().getInt("id");
        String title = response.jsonPath().getString("title");
        
        logger.info("Created new post with id={}", newPostId);
        assertTrue(newPostId > 0, "New post should have a valid ID");
        assertEquals(title, "Test Post Title", "Title should match the request");
    }

    @Test(groups = {"api", "regression"})
    @Story("PUT Requests")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that PUT /posts/{id} updates an existing post")
    public void testUpdatePost() {
        int postId = 1;
        logger.info("Starting test: testUpdatePost with id={}", postId);

        String requestBody = """
            {
                "id": 1,
                "title": "Updated Post Title",
                "body": "This is an updated post body",
                "userId": 1
            }
            """;

        Response response = given()
                .contentType(ContentType.JSON)
                .pathParam("id", postId)
                .body(requestBody)
            .when()
                .put("/posts/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        String updatedTitle = response.jsonPath().getString("title");
        logger.info("Updated post title to: {}", updatedTitle);
        assertEquals(updatedTitle, "Updated Post Title", "Title should be updated");
    }

    @Test(groups = {"api", "regression"})
    @Story("DELETE Requests")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that DELETE /posts/{id} deletes a post")
    public void testDeletePost() {
        int postId = 1;
        logger.info("Starting test: testDeletePost with id={}", postId);

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", postId)
            .when()
                .delete("/posts/{id}")
            .then()
                .statusCode(200);

        logger.info("Successfully deleted post with id={}", postId);
    }
}

