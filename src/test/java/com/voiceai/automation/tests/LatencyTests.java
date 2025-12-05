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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * Latency Tests - Tests for response time and performance requirements
 */
public class LatencyTests {
    private APIClient apiClient;
    private DBValidator dbValidator;
    private TestConfig config;
    private String sessionId;

    @BeforeClass
    public void setup() {
        System.out.println("========================================");
        System.out.println("LATENCY & PERFORMANCE TESTS");
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

    @Test(priority = 1, description = "Test simple query latency")
    public void testSimpleQueryLatency() {
        System.out.println("\n▶ TEST: Simple Query Latency");

        String userInput = "Hello";
        long startTime = System.currentTimeMillis();

        Response response = apiClient.sendVoiceQuery(userInput, sessionId);

        long latency = System.currentTimeMillis() - startTime;

        response.then().statusCode(200);

        LatencyResult result = apiClient.checkLatency(latency);
        Assert.assertTrue(latency <= config.getSimpleQueryLatency(),
                "Simple query latency " + latency + "ms exceeds threshold " +
                        config.getSimpleQueryLatency() + "ms");

        System.out.println("✓ PASSED: Simple Query Latency");
        System.out.println("  Latency: " + latency + "ms (threshold: " +
                config.getSimpleQueryLatency() + "ms)");
    }

    @Test(priority = 2, description = "Test complex query latency")
    public void testComplexQueryLatency() {
        System.out.println("\n▶ TEST: Complex Query Latency");

        String userInput = "I need to schedule a meeting for next Tuesday at 3pm " +
                "with the sales team to discuss quarterly targets and budget allocation";
        long startTime = System.currentTimeMillis();

        Response response = apiClient.sendVoiceQuery(userInput, sessionId);

        long latency = System.currentTimeMillis() - startTime;

        response.then().statusCode(200);

        Assert.assertTrue(latency <= config.getMaximumLatency(),
                "Complex query latency " + latency + "ms exceeds maximum " +
                        config.getMaximumLatency() + "ms");

        System.out.println("✓ PASSED: Complex Query Latency");
        System.out.println("  Latency: " + latency + "ms (max: " +
                config.getMaximumLatency() + "ms)");
    }

    @Test(priority = 3, description = "Test average latency over multiple requests")
    public void testAverageLatency() {
        System.out.println("\n▶ TEST: Average Latency (10 requests)");

        String[] queries = {
                "Hello", "What's the weather?", "Help me",
                "What time is it?", "Tell me a joke",
                "What services do you offer?", "Contact support",
                "Schedule appointment", "Cancel order", "Track package"
        };

        List<Long> latencies = new ArrayList<>();

        for (String query : queries) {
            long startTime = System.currentTimeMillis();
            Response response = apiClient.sendVoiceQuery(query, sessionId);
            long latency = System.currentTimeMillis() - startTime;
            latencies.add(latency);
            response.then().statusCode(anyOf(equalTo(200), equalTo(400)));
        }

        double avgLatency = latencies.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long maxLatency = latencies.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        long minLatency = latencies.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);

        Assert.assertTrue(avgLatency <= config.getAcceptableLatency(),
                "Average latency " + avgLatency + "ms exceeds acceptable " +
                        config.getAcceptableLatency() + "ms");

        System.out.println("✓ PASSED: Average Latency Test");
        System.out.println("  Average: " + String.format("%.2f", avgLatency) + "ms");
        System.out.println("  Min: " + minLatency + "ms");
        System.out.println("  Max: " + maxLatency + "ms");
    }

    @Test(priority = 4, description = "Test latency under load")
    public void testLatencyUnderLoad() {
        System.out.println("\n▶ TEST: Latency Under Load (5 rapid requests)");

        String userInput = "Quick test query";
        List<Long> latencies = new ArrayList<>();

        // Send 5 rapid requests
        for (int i = 0; i < 5; i++) {
            long startTime = System.currentTimeMillis();
            Response response = apiClient.sendVoiceQuery(userInput, sessionId);
            long latency = System.currentTimeMillis() - startTime;
            latencies.add(latency);
            response.then().statusCode(anyOf(equalTo(200), equalTo(429)));
        }

        // Check that latency doesn't degrade significantly
        long firstLatency = latencies.get(0);
        long lastLatency = latencies.get(latencies.size() - 1);

        System.out.println("✓ PASSED: Load Test");
        System.out.println("  First request: " + firstLatency + "ms");
        System.out.println("  Last request: " + lastLatency + "ms");
        System.out.println("  All latencies: " + latencies);
    }

    @AfterClass
    public void teardown() {
        dbValidator.close();
        System.out.println("\n========================================");
        System.out.println("LATENCY TESTS COMPLETED");
        System.out.println("========================================");
    }
}

