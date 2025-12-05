package com.voiceai.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceai.config.TestConfig;
import com.voiceai.models.LatencyResult;
import com.voiceai.models.VoiceRequest;
import com.voiceai.models.VoiceResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

/**
 * API Client utility for Voice AI API interactions
 */
public class APIClient {

    private static final Logger logger = LogManager.getLogger(APIClient.class);
    private final TestConfig config;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public APIClient() {
        this.config = TestConfig.getInstance();
        this.baseUrl = config.getBaseUrl();
        this.objectMapper = new ObjectMapper();

        RestAssured.baseURI = baseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        logger.info("APIClient initialized with base URL: {}", baseUrl);
    }

    /**
     * Create a new session
     */
    public Response createSession() {
        return getBaseRequest()
                .when()
                .post(config.getSessionEndpoint())
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    /**
     * Get a base request specification with common headers
     */
    private RequestSpecification getBaseRequest() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + config.getAuthToken());
    }

    /**
     * Send voice query and get response with latency measurement
     */
    public Response sendVoiceQuery(String userInput, String sessionId) {
        VoiceRequest request = new VoiceRequest(sessionId, userInput);

        long startTime = System.currentTimeMillis();

        Response response = getBaseRequest()
                .body(request)
                .when()
                .post(config.getChatEndpoint())
                .then()
                .extract()
                .response();

        long latency = System.currentTimeMillis() - startTime;

        // Store latency in response header for later validation
        System.out.println("Request latency: " + latency + "ms");

        return response;
    }

    /**
     * Send voice query with full validation chain
     */
    public Response sendVoiceQueryWithValidation(String userInput, String sessionId) {
        VoiceRequest request = new VoiceRequest(sessionId, userInput);

        return getBaseRequest()
                .body(request)
                .when()
                .post(config.getChatEndpoint())
                .then()
                .statusCode(200)
                .body("session_id", notNullValue())
                .body("response_text", notNullValue())
                .body("intent", notNullValue())
                .body("confidence", greaterThan(0.0f))
                .extract()
                .response();
    }

    /**
     * Get conversation history
     */
    public Response getConversationHistory(String sessionId) {
        return getBaseRequest()
                .queryParam("session_id", sessionId)
                .when()
                .get(config.getHistoryEndpoint())
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    /**
     * Validate response structure
     */
    public boolean validateResponseStructure(Response response) {
        try {
            response.then()
                    .body("response_text", notNullValue())
                    .body("intent", notNullValue())
                    .body("confidence", notNullValue())
                    .body("session_id", notNullValue());
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    /**
     * Extract response as VoiceResponse object
     */
    public VoiceResponse getVoiceResponse(Response response) {
        return response.as(VoiceResponse.class);
    }

    /**
     * Validate intent matches expected
     */
    public void validateIntent(Response response, String expectedIntent) {
        response.then()
                .body("intent", equalTo(expectedIntent));
    }

    /**
     * Validate confidence is above threshold
     */
    public void validateConfidence(Response response, double minConfidence) {
        response.then()
                .body("confidence", greaterThanOrEqualTo((float) minConfidence));
    }

    /**
     * Validate response contains keywords
     */
    public void validateKeywords(Response response, String... keywords) {
        String responseText = response.jsonPath().getString("response_text").toLowerCase();

        for (String keyword : keywords) {
            if (!responseText.contains(keyword.toLowerCase())) {
                throw new AssertionError("Response doesn't contain keyword: " + keyword);
            }
        }
    }

    /**
     * Check latency is within threshold
     */
    public LatencyResult checkLatency(long latencyMs) {
        return new LatencyResult(
                latencyMs,
                latencyMs <= config.getAcceptableLatency(),
                latencyMs <= config.getMaximumLatency());
    }

    // ==================== JSON SCHEMA VALIDATION ====================

    /**
     * Validate response against Voice Response JSON schema
     */
    public void validateVoiceResponseSchema(Response response) {
        response.then()
                .body(matchesJsonSchemaInClasspath("schemas/voice-response-schema.json"));
        logger.info("Voice response schema validation passed");
    }

    /**
     * Validate session response against Session Response JSON schema
     */
    public void validateSessionResponseSchema(Response response) {
        response.then()
                .body(matchesJsonSchemaInClasspath("schemas/session-response-schema.json"));
        logger.info("Session response schema validation passed");
    }

    /**
     * Send voice query with JSON schema validation
     */
    public Response sendVoiceQueryWithSchemaValidation(String userInput, String sessionId) {
        VoiceRequest request = new VoiceRequest(sessionId, userInput);

        Response response = getBaseRequest()
                .body(request)
                .when()
                .post(config.getChatEndpoint())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/voice-response-schema.json"))
                .extract()
                .response();

        logger.info("Voice query with schema validation completed successfully");
        return response;
    }

    /**
     * Create session with JSON schema validation
     */
    public Response createSessionWithSchemaValidation() {
        Response response = getBaseRequest()
                .when()
                .post(config.getSessionEndpoint())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/session-response-schema.json"))
                .extract()
                .response();

        logger.info("Session created with schema validation");
        return response;
    }

}
