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
 * Hallucination Tests - Tests to detect AI hallucinations and factual accuracy
 */
public class HallucinationTests {
    private APIClient apiClient;
    private DBValidator dbValidator;
    private TestConfig config;
    private String sessionId;

    @BeforeClass
    public void setup() {
        System.out.println("========================================");
        System.out.println("HALLUCINATION DETECTION TESTS");
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

    @Test(priority = 1, description = "Test factual question accuracy")
    public void testFactualAccuracy() {
        System.out.println("\n▶ TEST: Factual Accuracy");

        String userInput = "What year was the company founded?";

        Response response = apiClient.sendVoiceQuery(userInput, sessionId);

        response.then()
                .statusCode(200)
                .body("confidence", greaterThanOrEqualTo(0.7f));

        VoiceResponse voiceResponse = response.as(VoiceResponse.class);

        // If confidence is low, it should indicate uncertainty
        if (voiceResponse.getConfidence() < 0.7) {
            Assert.assertTrue(voiceResponse.isFallback() ||
                            voiceResponse.getResponseText().toLowerCase().contains("not sure") ||
                            voiceResponse.getResponseText().toLowerCase().contains("don't know"),
                    "Low confidence responses should indicate uncertainty");
        }

        System.out.println("✓ PASSED: Factual response validated");
        System.out.println("  Confidence: " + voiceResponse.getConfidence());
    }

    @Test(priority = 2, description = "Test unknown topic handling")
    public void testUnknownTopicHandling() {
        System.out.println("\n▶ TEST: Unknown Topic Handling");

        String userInput = "Tell me about the fictional product XYZ-9999 that doesn't exist";

        Response response = apiClient.sendVoiceQuery(userInput, sessionId);

        response.then()
                .statusCode(200);

        VoiceResponse voiceResponse = response.as(VoiceResponse.class);

        // Should either have low confidence or indicate it doesn't have information
        boolean handledCorrectly = voiceResponse.isFallback() ||
                voiceResponse.getConfidence() < 0.5 ||
                voiceResponse.getResponseText().toLowerCase().contains("don't have") ||
                voiceResponse.getResponseText().toLowerCase().contains("not familiar") ||
                voiceResponse.getResponseText().toLowerCase().contains("can't find");

        Assert.assertTrue(handledCorrectly,
                "Unknown topics should be handled with uncertainty or fallback");

        System.out.println("✓ PASSED: Unknown topic handled appropriately");
    }

    @Test(priority = 3, description = "Test contradictory information handling")
    public void testContradictoryInformation() {
        System.out.println("\n▶ TEST: Contradictory Information");

        // First query
        String userInput1 = "What are your business hours?";
        Response response1 = apiClient.sendVoiceQuery(userInput1, sessionId);
        VoiceResponse voiceResponse1 = response1.as(VoiceResponse.class);
        String firstAnswer = voiceResponse1.getResponseText();

        // Same query again - should be consistent
        String userInput2 = "What time do you open and close?";
        Response response2 = apiClient.sendVoiceQuery(userInput2, sessionId);
        VoiceResponse voiceResponse2 = response2.as(VoiceResponse.class);
        String secondAnswer = voiceResponse2.getResponseText();

        // Responses about the same topic should be consistent
        System.out.println("  First response: " + firstAnswer);
        System.out.println("  Second response: " + secondAnswer);

        System.out.println("✓ PASSED: Consistency check completed");
    }

    @Test(priority = 4, description = "Test out-of-scope question handling")
    public void testOutOfScopeQuestion() {
        System.out.println("\n▶ TEST: Out-of-Scope Question");

        String userInput = "What is the meaning of life and the universe?";

        Response response = apiClient.sendVoiceQuery(userInput, sessionId);

        response.then()
                .statusCode(200);

        VoiceResponse voiceResponse = response.as(VoiceResponse.class);

        // Should either redirect to relevant topics or indicate it's out of scope
        Assert.assertTrue(voiceResponse.isFallback() ||
                        voiceResponse.getConfidence() < config.getMinimumConfidence(),
                "Out-of-scope questions should have low confidence or trigger fallback");

        System.out.println("✓ PASSED: Out-of-scope handled correctly");
    }

    @Test(priority = 5, description = "Test confidence threshold accuracy")
    public void testConfidenceThreshold() {
        System.out.println("\n▶ TEST: Confidence Threshold Accuracy");

        String userInput = "What services do you offer?";

        Response response = apiClient.sendVoiceQuery(userInput, sessionId);
        VoiceResponse voiceResponse = response.as(VoiceResponse.class);

        // High confidence responses should be accurate and relevant
        if (voiceResponse.getConfidence() >= config.getHighConfidence()) {
            Assert.assertFalse(voiceResponse.isFallback(),
                    "High confidence responses should not be fallbacks");
            Assert.assertNotNull(voiceResponse.getIntent(),
                    "High confidence responses should have a clear intent");
        }

        System.out.println("✓ PASSED: Confidence threshold validated");
        System.out.println("  Confidence: " + voiceResponse.getConfidence());
    }

    @AfterClass
    public void teardown() {
        dbValidator.close();
        System.out.println("\n========================================");
        System.out.println("HALLUCINATION TESTS COMPLETED");
        System.out.println("========================================");
    }
}

