package com.voiceai.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Voice Response Model - POJO for Voice AI API responses
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceResponse {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("response_text")
    private String responseText;

    @JsonProperty("intent")
    private String intent;

    @JsonProperty("confidence")
    private double confidence;

    @JsonProperty("response_time_ms")
    private long responseTimeMs;

    @JsonProperty("is_fallback")
    private boolean isFallback;

    @JsonProperty("entities")
    private Map<String, Object> entities;

    @JsonProperty("status")
    private String status;

    @JsonProperty("error")
    private String error;

    // Default constructor required for Jackson deserialization
    public VoiceResponse() {
    }

    // Getters
    public String getSessionId() {
        return sessionId;
    }

    public String getResponseText() {
        return responseText;
    }

    public String getIntent() {
        return intent;
    }

    public double getConfidence() {
        return confidence;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public boolean isFallback() {
        return isFallback;
    }

    public Map<String, Object> getEntities() {
        return entities;
    }

    public String getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    // Setters
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public void setFallback(boolean fallback) {
        isFallback = fallback;
    }

    public void setEntities(Map<String, Object> entities) {
        this.entities = entities;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setError(String error) {
        this.error = error;
    }

    // Builder-style methods for fluent API
    public VoiceResponse withSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public VoiceResponse withResponseText(String responseText) {
        this.responseText = responseText;
        return this;
    }

    public VoiceResponse withIntent(String intent) {
        this.intent = intent;
        return this;
    }

    public VoiceResponse withConfidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

    public VoiceResponse withResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
        return this;
    }

    public VoiceResponse withFallback(boolean fallback) {
        this.isFallback = fallback;
        return this;
    }

    public VoiceResponse withEntities(Map<String, Object> entities) {
        this.entities = entities;
        return this;
    }

    public VoiceResponse withStatus(String status) {
        this.status = status;
        return this;
    }

    public VoiceResponse withError(String error) {
        this.error = error;
        return this;
    }

    // Helper methods
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status) && error == null;
    }

    public boolean hasError() {
        return error != null && !error.isEmpty();
    }

    @Override
    public String toString() {
        return "VoiceResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", responseText='" + responseText + '\'' +
                ", intent='" + intent + '\'' +
                ", confidence=" + confidence +
                ", responseTimeMs=" + responseTimeMs +
                ", isFallback=" + isFallback +
                ", entities=" + entities +
                ", status='" + status + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
