package com.voiceai.automation.tests;

import com.voiceai.config.TestConfig;
import com.voiceai.models.LatencyResult;
import com.voiceai.models.VoiceResponse;
import com.voiceai.utils.APIClient;
import com.voiceai.utils.DBValidator;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;

public class NormalConversationTests {
    private APIClient apiClient;
    private DBValidator dbValidator;
    private TestConfig config;
    private String sessionId;

    @BeforeClass
    public void setup() {
        System.out.println("========================================");
        System.out.println("VOICE AI AUTOMATION TEST SUITE");
        System.out.println("========================================\n");
        System.out.println("Setting up test environment...");
        apiClient = new APIClient();
        dbValidator = new DBValidator();
        config = TestConfig.getInstance();

    }

    @BeforeMethod
    public void createSession() {
        // Create new session before each test
        Response sessionResponse = apiClient.createSession();
        sessionId = sessionResponse.jsonPath().getString("session_id");
        Assert.assertNotNull(sessionId, "Session ID should not be null");
        Assert.assertFalse(sessionId.isEmpty(), "Session ID should not be empty");
        System.out.println("\n✓ New session created: " + sessionId);
    }

    // ===== TEST 1: WEATHER QUERY =====

    @Test(priority = 1, description = "Test weather query with REST Assured")
    public void testWeatherQuery() {
        System.out.println("\n▶ TEST: Weather Query");

        String userInput = "What's the weather today?";
        String expectedIntent = "weather_query";

        // Measure latency
        long startTime = System.currentTimeMillis();

        // Send request and validate using REST Assured
        Response response = apiClient.sendVoiceQuery(userInput, sessionId);

        long latency = System.currentTimeMillis() - startTime;

        // REST Assured Validations
        response.then()
                .statusCode(200)
                .body("intent", equalTo(expectedIntent))
                .body("confidence", greaterThanOrEqualTo(0.8f))
                .body("response_text", notNullValue())
                .body("response_text", containsStringIgnoringCase("weather"));

        // Extract response object
        VoiceResponse voiceResponse = response.as(VoiceResponse.class);

        // Validate confidence
        Assert.assertTrue(voiceResponse.getConfidence() >= 0.8,
                "Confidence should be >= 0.8");

        // Validate latency
        LatencyResult latencyResult = apiClient.checkLatency(latency);
        Assert.assertTrue(latencyResult.isWithinThreshold(),
                "Latency " + latency + "ms exceeds threshold");

        // Wait for database write
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        // Validate database logging
        DBValidator.ConversationRecord record = dbValidator.verifyConversationLogged(sessionId, userInput);
        Assert.assertNotNull(record, "Conversation should be logged in database");
        Assert.assertEquals(record.getIntent(), expectedIntent,
                "Intent in DB should match expected");

        System.out.println("✓ PASSED: Weather Query");
        System.out.println("  Intent: " + voiceResponse.getIntent());
        System.out.println("  Confidence: " + voiceResponse.getConfidence());
        System.out.println("  Latency: " + latency + "ms");
        System.out.println("  Response: " + voiceResponse.getResponseText());
    }

    @AfterClass
    public void teardown() {
        dbValidator.close();
        System.out.println("\n========================================");
        System.out.println("ALL TESTS COMPLETED");
        System.out.println("========================================");
    }

}
