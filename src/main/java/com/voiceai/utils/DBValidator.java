package com.voiceai.utils;

import com.voiceai.config.*;
import java.sql.*;

/**
 * Database Validator for Voice AI Testing
 */
public class DBValidator {

    private Connection connection;
    private TestConfig config;

    public DBValidator() {
        this.config = TestConfig.getInstance();
        connect();
    }

    /**
     * Establish database connection
     */
    public boolean connect() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(
                    config.getDbUrl(),
                    config.getDbUsername(),
                    config.getDbPassword());
            return true;
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Close database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Verify conversation was logged in database
     */
    public ConversationRecord verifyConversationLogged(String sessionId, String userInput) {
        String query = "SELECT * FROM conversations " +
                "WHERE session_id = ? AND user_input = ? " +
                "ORDER BY created_at DESC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, sessionId);
            stmt.setString(2, userInput);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new ConversationRecord(
                        rs.getLong("id"),
                        rs.getString("session_id"),
                        rs.getString("user_input"),
                        rs.getString("ai_response"),
                        rs.getString("intent"),
                        rs.getDouble("confidence"),
                        rs.getTimestamp("created_at"),
                        rs.getBoolean("is_fallback"));
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Error verifying conversation: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verify intent was captured correctly
     */
    public boolean verifyIntentCaptured(String sessionId, String expectedIntent) {
        String query = "SELECT intent FROM conversations " +
                "WHERE session_id = ? " +
                "ORDER BY created_at DESC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, sessionId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String capturedIntent = rs.getString("intent");
                return expectedIntent.equals(capturedIntent);
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error verifying intent: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get session metrics
     */
    public SessionMetrics getSessionMetrics(String sessionId) {
        String query = "SELECT " +
                "COUNT(*) as total_interactions, " +
                "AVG(response_time_ms) as avg_response_time, " +
                "AVG(confidence) as avg_confidence " +
                "FROM conversations " +
                "WHERE session_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, sessionId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new SessionMetrics(
                        rs.getInt("total_interactions"),
                        rs.getDouble("avg_response_time"),
                        rs.getDouble("avg_confidence"));
            }

            return new SessionMetrics(0, 0.0, 0.0);

        } catch (SQLException e) {
            System.err.println("Error getting metrics: " + e.getMessage());
            return new SessionMetrics(0, 0.0, 0.0);
        }
    }

    /**
     * Verify fallback was logged
     */
    public boolean verifyFallbackLogged(String sessionId) {
        String query = "SELECT is_fallback FROM conversations " +
                "WHERE session_id = ? " +
                "ORDER BY created_at DESC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, sessionId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_fallback");
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error checking fallback: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all conversations for session
     */
    public int getConversationCount(String sessionId) {
        String query = "SELECT COUNT(*) as count FROM conversations WHERE session_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, sessionId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }

            return 0;

        } catch (SQLException e) {
            System.err.println("Error getting conversation count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Inner class for conversation record
     */
    public static class ConversationRecord {
        private long id;
        private String sessionId;
        private String userInput;
        private String aiResponse;
        private String intent;
        private double confidence;
        private Timestamp createdAt;
        private boolean isFallback;

        public ConversationRecord(long id, String sessionId, String userInput,
                String aiResponse, String intent, double confidence,
                Timestamp createdAt, boolean isFallback) {
            this.id = id;
            this.sessionId = sessionId;
            this.userInput = userInput;
            this.aiResponse = aiResponse;
            this.intent = intent;
            this.confidence = confidence;
            this.createdAt = createdAt;
            this.isFallback = isFallback;
        }

        // Getters
        public long getId() {
            return id;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getUserInput() {
            return userInput;
        }

        public String getAiResponse() {
            return aiResponse;
        }

        public String getIntent() {
            return intent;
        }

        public double getConfidence() {
            return confidence;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }

        public boolean isFallback() {
            return isFallback;
        }
    }

    /**
     * Inner class for session metrics
     */
    public static class SessionMetrics {
        private int totalInteractions;
        private double avgResponseTime;
        private double avgConfidence;

        public SessionMetrics(int totalInteractions, double avgResponseTime, double avgConfidence) {
            this.totalInteractions = totalInteractions;
            this.avgResponseTime = avgResponseTime;
            this.avgConfidence = avgConfidence;
        }

        // Getters
        public int getTotalInteractions() {
            return totalInteractions;
        }

        public double getAvgResponseTime() {
            return avgResponseTime;
        }

        public double getAvgConfidence() {
            return avgConfidence;
        }
    }
}
