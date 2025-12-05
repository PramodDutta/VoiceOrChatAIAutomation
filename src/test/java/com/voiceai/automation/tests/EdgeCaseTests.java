package com.voiceai.automation.tests;

import com.voiceai.config.TestConfig;
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

/**
 * Edge Case Tests - Tests for unusual inputs, boundary conditions, and error handling
 */
public class EdgeCaseTests {
    private APIClient apiClient;
    private DBValidator dbValidator;
    private TestConfig config;
    private String sessionId;

    @BeforeClass
    public void setup() {
        System.out.println("========================================");
        System.out.println("EDGE CASE TESTS");
        System.out.println("========================================\n");
        apiClient = new APIClient();
        dbValidator = new DBValidator();
        config = TestConfig.getInstance();
    }

    @BeforeMethod
    public void createSession() {
        Response sessionResponse = apiClient.createSession();
        sessionId = sessionResponse.jsonPath().getString("session_id");
        Assert.assertNotNull(sessionId, "Session ID should not be null");
        System.out.println("\n✓ New session created: " + sessionId);
    }

    @Test(priority = 1, description = "Test empty input handling")
    public void testEmptyInput() {
        System.out.println("\n▶ TEST: Empty Input");

        String userInput = "";

        Response response = apiClient.sendVoiceQuery(userInput, sessionId);

        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(400)))
                .body("is_fallback", equalTo(true));

        VoiceResponse voiceResponse = response.as(VoiceResponse.class);
        Assert.assertTrue(voiceResponse.isFallback(), "Empty input should trigger fallback");

        System.out.println("✓ PASSED: Empty Input handled correctly");
    }

    @Test(priority = 2, description = "Test very long input handling")
    public void testVeryLongInput() {
        System.out.println("\n▶ TEST: Very Long Input");

        // Create a 5000 character input
        String userInput = "What is the weather ".repeat(250);

        Response response = apiClient.sendVoiceQuery(userInput, sessionId);

        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(413)));

        System.out.println("✓ PASSED: Long input handled without server crash");
    }

    @Test(priority = 3, description = "Test special characters in input")
    public void testSpecialCharacters() {
        System.out.println("\n▶ TEST: Special Characters");

        String userInput = "What's the weather? <script>alert('test')</script> @#$%^&*()";

        Response response = apiClient.sendVoiceQuery(userInput, sessionId);

        response.then()
                .statusCode(200)
                .body("response_text", notNullValue());

        VoiceResponse voiceResponse = response.as(VoiceResponse.class);
        // Ensure no script injection in response
        Assert.assertFalse(voiceResponse.getResponseText().contains("<script>"),
                "Response should not contain injected scripts");

        System.out.println("✓ PASSED: Special characters handled safely");
    }

    @Test(priority = 4, description = "Test Unicode and emoji handling")
    public void testUnicodeAndEmoji() {
        System.out.println("\n▶ TEST: Unicode and Emoji");

        String userInput = "What's the weather today? 🌤️ ñ é ü 中文";

        Response response = apiClient.sendVoiceQuery(userInput, sessionId);

        response.then()
                .statusCode(200)
                .body("response_text", notNullValue());

        System.out.println("✓ PASSED: Unicode and emoji handled correctly");
    }

    @Test(priority = 5, description = "Test SQL injection prevention")
    public void testSQLInjectionPrevention() {
        System.out.println("\n▶ TEST: SQL Injection Prevention");

        String userInput = "'; DROP TABLE conversations; --";

        Response response = apiClient.sendVoiceQuery(userInput, sessionId);

        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(400)));

        // Verify database is still accessible
        int count = dbValidator.getConversationCount(sessionId);
        Assert.assertTrue(count >= 0, "Database should still be accessible");

        System.out.println("✓ PASSED: SQL injection attempt handled safely");
    }

    @Test(priority = 6, description = "Test invalid session ID")
    public void testInvalidSessionId() {
        System.out.println("\n▶ TEST: Invalid Session ID");

        String invalidSessionId = "invalid-session-12345";
        String userInput = "Hello";

        Response response = apiClient.sendVoiceQuery(userInput, invalidSessionId);

        response.then()
                .statusCode(anyOf(equalTo(400), equalTo(401), equalTo(404)));

        System.out.println("✓ PASSED: Invalid session ID rejected");
    }

    @AfterClass
    public void teardown() {
        dbValidator.close();
        System.out.println("\n========================================");
        System.out.println("EDGE CASE TESTS COMPLETED");
        System.out.println("========================================");
    }
}

