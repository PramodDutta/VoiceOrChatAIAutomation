package com.voiceai.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Voice Request Model - POJO for Voice AI API requests
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceRequest {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_input")
    private String userInput;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("voice_data")
    private String voiceData;

    // Default constructor required for Jackson deserialization
    public VoiceRequest() {
    }

    // All-args constructor
    public VoiceRequest(String sessionId, String userInput, long timestamp, String voiceData) {
        this.sessionId = sessionId;
        this.userInput = userInput;
        this.timestamp = timestamp;
        this.voiceData = voiceData;
    }

    public VoiceRequest(String sessionId, String userInput) {
        this.sessionId = sessionId;
        this.userInput = userInput;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getSessionId() {
        return sessionId;
    }

    public String getUserInput() {
        return userInput;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getVoiceData() {
        return voiceData;
    }

    // Setters
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setVoiceData(String voiceData) {
        this.voiceData = voiceData;
    }

    // Builder pattern for fluent API
    public VoiceRequest withSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public VoiceRequest withUserInput(String userInput) {
        this.userInput = userInput;
        return this;
    }

    public VoiceRequest withTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public VoiceRequest withVoiceData(String voiceData) {
        this.voiceData = voiceData;
        return this;
    }

    @Override
    public String toString() {
        return "VoiceRequest{" +
                "sessionId='" + sessionId + '\'' +
                ", userInput='" + userInput + '\'' +
                ", timestamp=" + timestamp +
                ", voiceData='" + voiceData + '\'' +
                '}';
    }
}
