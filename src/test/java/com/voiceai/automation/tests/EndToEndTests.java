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
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;

/**
 * End-to-End Tests - Complete workflow tests covering full user journeys
 */
public class EndToEndTests {
    private APIClient apiClient;
    private DBValidator dbValidator;
    private TestConfig config;
    private String sessionId;

    @BeforeClass
    public void setup() {
        System.out.println("========================================");
        System.out.println("END-TO-END WORKFLOW TESTS");
        System.out.println("========================================\n");
        apiClient = new APIClient();
        dbValidator = new DBValidator();
        config = TestConfig.getInstance();
    }

    @Test(priority = 1, description = "Complete customer support workflow")
    public void testCustomerSupportWorkflow() {
        System.out.println("\n▶ TEST: Customer Support Workflow");

        // Step 1: Create session
        System.out.println("  Step 1: Creating session...");
        Response sessionResponse = apiClient.createSession();
        sessionId = sessionResponse.jsonPath().getString("session_id");
        Assert.assertNotNull(sessionId, "Session created");
        System.out.println("    ✓ Session created: " + sessionId);

        // Step 2: Initial greeting
        System.out.println("  Step 2: Initial greeting...");
        Response greetingResponse = apiClient.sendVoiceQuery("Hello, I need help", sessionId);
        greetingResponse.then()
                .statusCode(200)
                .body("response_text", notNullValue());
        System.out.println("    ✓ Greeting response received");

        // Step 3: Specific query
        System.out.println("  Step 3: Specific query...");
        Response queryResponse = apiClient.sendVoiceQuery(
                "I want to check my order status", sessionId);
        queryResponse.then()
                .statusCode(200)
                .body("intent", notNullValue());
        VoiceResponse queryVoice = queryResponse.as(VoiceResponse.class);
        System.out.println("    ✓ Query processed, intent: " + queryVoice.getIntent());

        // Step 4: Follow-up question
        System.out.println("  Step 4: Follow-up question...");
        Response followUpResponse = apiClient.sendVoiceQuery(
                "When will it arrive?", sessionId);
        followUpResponse.then().statusCode(200);
        System.out.println("    ✓ Follow-up handled");

        // Step 5: Verify conversation history
        System.out.println("  Step 5: Verifying conversation history...");
        Response historyResponse = apiClient.getConversationHistory(sessionId);
        historyResponse.then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(3));
        System.out.println("    ✓ History contains all interactions");

        // Step 6: Verify database logging
        System.out.println("  Step 6: Verifying database...");
        int conversationCount = dbValidator.getConversationCount(sessionId);
        Assert.assertTrue(conversationCount >= 3,
                "All conversations should be logged");
        System.out.println("    ✓ Database verified: " + conversationCount + " records");

        System.out.println("✓ PASSED: Customer Support Workflow");
    }

    @Test(priority = 2, description = "Multi-turn conversation test")
    public void testMultiTurnConversation() {
        System.out.println("\n▶ TEST: Multi-Turn Conversation");

        // Create session
        Response sessionResponse = apiClient.createSession();
        sessionId = sessionResponse.jsonPath().getString("session_id");

        String[] conversation = {
                "Hi there",
                "I need to book an appointment",
                "Tomorrow at 2pm",
                "Yes, that works",
                "Thank you"
        };

        for (int i = 0; i < conversation.length; i++) {
            System.out.println("  Turn " + (i + 1) + ": " + conversation[i]);
            Response response = apiClient.sendVoiceQuery(conversation[i], sessionId);
            response.then().statusCode(200);

            VoiceResponse voiceResponse = response.as(VoiceResponse.class);
            System.out.println("    Response: " + truncate(voiceResponse.getResponseText(), 50));
        }

        // Verify session maintains context
        DBValidator.SessionMetrics metrics = dbValidator.getSessionMetrics(sessionId);
        Assert.assertEquals(metrics.getTotalInteractions(), conversation.length,
                "All turns should be recorded");

        System.out.println("✓ PASSED: Multi-Turn Conversation");
        System.out.println("  Total turns: " + metrics.getTotalInteractions());
        System.out.println("  Avg confidence: " + String.format("%.2f", metrics.getAvgConfidence()));
    }

    @Test(priority = 3, description = "Error recovery workflow")
    public void testErrorRecoveryWorkflow() {
        System.out.println("\n▶ TEST: Error Recovery Workflow");

        Response sessionResponse = apiClient.createSession();
        sessionId = sessionResponse.jsonPath().getString("session_id");

        // Step 1: Valid query
        Response validResponse = apiClient.sendVoiceQuery("Hello", sessionId);
        validResponse.then().statusCode(200);
        System.out.println("  ✓ Valid query succeeded");

        // Step 2: Trigger fallback
        Response fallbackResponse = apiClient.sendVoiceQuery("asdfghjkl random gibberish", sessionId);
        fallbackResponse.then().statusCode(200);
        VoiceResponse fallbackVoice = fallbackResponse.as(VoiceResponse.class);
        System.out.println("  ✓ Fallback triggered: " + fallbackVoice.isFallback());

        // Step 3: Recover with valid query
        Response recoveryResponse = apiClient.sendVoiceQuery("What can you help me with?", sessionId);
        recoveryResponse.then()
                .statusCode(200)
                .body("is_fallback", equalTo(false));
        System.out.println("  ✓ Recovery successful");

        System.out.println("✓ PASSED: Error Recovery Workflow");
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    @AfterClass
    public void teardown() {
        dbValidator.close();
        System.out.println("\n========================================");
        System.out.println("END-TO-END TESTS COMPLETED");
        System.out.println("========================================");
    }
}

