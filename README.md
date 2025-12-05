# VoiceAI Automation Toolkit

A comprehensive Voice AI REST API Test Automation Framework built with Maven, TestNG, REST Assured, Jackson, and Allure Reporting.

## 👤 Author

**Name:** Pramod Dutta
**Website:** [The Testing Academy](https://thetestingacademy.com)

---

## 🚀 Features

- **REST Assured** for API testing with fluent assertions
- **Jackson** for JSON serialization/deserialization
- **TestNG** as the test framework with parallel execution support
- **Allure** for beautiful, interactive test reports
- **Log4j 2** for comprehensive logging
- **PostgreSQL** integration for database validation
- **Hamcrest** matchers for expressive assertions

---

## 🎯 Core Objectives

### 1. Test AI APIs

- Call APIs that trigger AI responses (like sending a message to a chatbot)
- Verify the AI returns expected responses
- Check response format, status codes, and data structure

### 2. Simulate User Interactions

- Create test scenarios mimicking real user inputs
- Test different conversation flows
- Validate AI understands and responds correctly to various queries

### 3. End-to-End Validation

Verify the complete flow:

```
User Input → API Call → AI Processing → Response → Logs → Database
```

**Validation Checklist:**
| Component | What to Check |
|-----------|---------------|
| **API** | Request/response working correctly |
| **Logs** | System logs the interaction properly |
| **Database** | Data is saved correctly (conversation history, user data, etc.) |

### 4. Handle AI-Specific Edge Cases

#### 🔮 Hallucinations
AI making up false information

- **Test:** Verify AI doesn't invent facts
- **Example:** Ask *"What's the capital of Mars?"* - should say *"Mars has no capital"* not make one up

#### 🔄 Fallback Handling
What happens when AI doesn't understand

- **Test:** Send gibberish or unexpected inputs
- **Verify:** System returns appropriate fallback message (*"I don't understand, can you rephrase?"*)

#### ⏱️ Latency
Response time issues

- **Test:** Measure how long responses take
- **Verify:** Responses come within acceptable timeframes (e.g., < 3 seconds)

---

## Prerequisites

- **Java 11** or higher
- **Maven 3.6+**
- **Allure Command Line** (for viewing reports locally)

### Installing Allure CLI

```bash
# macOS
brew install allure

# Windows (using Scoop)
scoop install allure

# Linux
sudo apt-add-repository ppa:qameta/allure
sudo apt-get update
sudo apt-get install allure
```

## Project Structure

```
VoiceAIAutomationTK/
├── pom.xml                              # Maven configuration with dependencies
├── testng.xml                           # TestNG suite configuration
├── README.md                            # This file
├── src/
│   ├── main/java/com/voiceai/
│   │   ├── config/
│   │   │   └── TestConfig.java          # Configuration manager (singleton)
│   │   ├── models/
│   │   │   ├── VoiceRequest.java        # Voice request POJO (Jackson)
│   │   │   ├── VoiceResponse.java       # Voice response POJO (Jackson)
│   │   │   └── LatencyResult.java       # Latency result model
│   │   └── utils/
│   │       ├── APIClient.java           # REST API client utility
│   │       └── DBValidator.java         # Database validation utility
│   └── test/
│       ├── java/com/voiceai/automation/tests/
│       │   ├── SampleApiTest.java       # Sample API tests
│       │   ├── NormalConversationTests.java
│       │   ├── EdgeCaseTests.java
│       │   ├── HallucinationTests.java
│       │   ├── LatencyTests.java
│       │   └── EndToEndTests.java
│       └── resources/
│           ├── config.properties        # Test configuration
│           └── log4j2.xml               # Logging configuration
└── target/                              # Build output (generated)
    ├── allure-results/                  # Allure raw results
    └── allure-report/                   # Allure HTML report
```

## Running Tests

### Run All Tests

```bash
mvn clean test
```

### Run Specific Test Class

```bash
mvn clean test -Dtest=SampleApiTest
```

### Run Tests by Group

```bash
# Run smoke tests
mvn clean test -Dgroups=smoke

# Run regression tests
mvn clean test -Dgroups=regression
```

### Run with Custom TestNG Suite

```bash
mvn clean test -DsuiteXmlFile=testng.xml
```

## Generating Allure Reports

### Generate and Open Report

```bash
# Run tests first
mvn clean test

# Generate and serve report (opens in browser)
mvn allure:serve
```

### Generate Report Only (without serving)

```bash
mvn allure:report
```

The report will be generated in `target/allure-report/`.

### View Existing Report

```bash
allure open target/allure-report
```

## Configuration Files

### pom.xml
Contains all Maven dependencies and plugin configurations:
- REST Assured 5.4.0
- TestNG 7.9.0
- Allure TestNG 2.25.0
- Log4j 2.22.1
- Jackson 2.16.1

### testng.xml
TestNG suite configuration file defining:
- Test suites and their configurations
- Allure listener for report generation
- Test class/package organization

### log4j2.xml
Logging configuration with:
- Console output
- Rolling file appender for logs
- Separate log levels for different packages

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| REST Assured | 5.4.0 | API Testing |
| REST Assured JSON Schema Validator | 5.4.0 | JSON Schema Validation |
| TestNG | 7.9.0 | Test Framework |
| Allure TestNG | 2.25.0 | Reporting |
| Log4j 2 | 2.22.1 | Logging |
| Jackson | 2.16.1 | JSON Processing |
| PostgreSQL | 42.6.0 | Database Connectivity |
| Hamcrest | 2.2 | Matchers & Assertions |

---

## 📝 JSON Schema Validation

This framework includes JSON Schema validation using REST Assured's `json-schema-validator` module.

### Schema Files Location
```
src/test/resources/schemas/
├── voice-request-schema.json      # Request payload schema
├── voice-response-schema.json     # Response payload schema
└── session-response-schema.json   # Session creation response schema
```

### Sample Request

**POST** `/api/v1/chat`

```json
{
  "session_id": "sess_abc123xyz789",
  "user_input": "What's the weather like today?",
  "timestamp": "2024-01-15T10:30:00Z",
  "voice_data": null
}
```

### Sample Response

```json
{
  "session_id": "sess_abc123xyz789",
  "response_text": "The weather today is sunny with a high of 72°F and a low of 58°F. Perfect day to be outside!",
  "intent": "weather_query",
  "confidence": 0.95,
  "response_time_ms": 245,
  "is_fallback": false,
  "entities": {
    "location": "current",
    "time_period": "today"
  },
  "status": "success",
  "error": null
}
```

### JSON Schema Validation Usage

```java
// Validate response against schema
apiClient.validateVoiceResponseSchema(response);

// Send query with automatic schema validation
Response response = apiClient.sendVoiceQueryWithSchemaValidation(userInput, sessionId);

// Create session with schema validation
Response sessionResponse = apiClient.createSessionWithSchemaValidation();

// Manual schema validation in tests
response.then()
    .body(matchesJsonSchemaInClasspath("schemas/voice-response-schema.json"));
```

### Voice Response Schema

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "VoiceResponse",
  "type": "object",
  "required": ["session_id", "response_text", "intent", "confidence"],
  "properties": {
    "session_id": { "type": "string", "minLength": 1 },
    "response_text": { "type": "string" },
    "intent": {
      "type": "string",
      "enum": ["greeting", "weather_query", "appointment_booking",
               "order_status", "support_request", "general_query",
               "fallback", "goodbye"]
    },
    "confidence": { "type": "number", "minimum": 0.0, "maximum": 1.0 },
    "response_time_ms": { "type": "integer", "minimum": 0 },
    "is_fallback": { "type": "boolean" },
    "entities": { "type": ["object", "null"] },
    "status": { "type": "string", "enum": ["success", "error", "partial"] },
    "error": { "type": ["string", "null"] }
  }
}
```

---

## 📋 Test Cases Covered

### 1. Normal Conversation Tests (`NormalConversationTests.java`)
| Test Case | Description |
|-----------|-------------|
| `testWeatherQuery` | Tests weather query with REST Assured validation, confidence check, latency measurement, and database logging verification |

### 2. Edge Case Tests (`EdgeCaseTests.java`)
| Test Case | Description |
|-----------|-------------|
| `testEmptyInput` | Tests handling of empty input - should trigger fallback |
| `testVeryLongInput` | Tests handling of very long input (5000 characters) |
| `testSpecialCharacters` | Tests special characters and XSS injection prevention |
| `testUnicodeAndEmoji` | Tests Unicode characters and emoji handling |
| `testSQLInjectionPrevention` | Tests SQL injection prevention in input |
| `testInvalidSessionId` | Tests rejection of invalid session IDs |

### 3. Hallucination Tests (`HallucinationTests.java`)
| Test Case | Description |
|-----------|-------------|
| `testFactualAccuracy` | Tests factual question accuracy and confidence levels |
| `testUnknownTopicHandling` | Tests handling of unknown/fictional topics |
| `testContradictoryInformation` | Tests consistency across similar queries |
| `testOutOfScopeQuestion` | Tests handling of out-of-scope questions |
| `testConfidenceThreshold` | Tests confidence threshold accuracy |

### 4. Latency Tests (`LatencyTests.java`)
| Test Case | Description |
|-----------|-------------|
| `testSimpleQueryLatency` | Tests simple query response time against threshold |
| `testComplexQueryLatency` | Tests complex query response time against maximum limit |
| `testAverageLatency` | Tests average latency over 10 different requests |
| `testLatencyUnderLoad` | Tests latency degradation under rapid consecutive requests |

### 5. End-to-End Tests (`EndToEndTests.java`)
| Test Case | Description |
|-----------|-------------|
| `testCustomerSupportWorkflow` | Complete customer support workflow: session creation → greeting → query → follow-up → history verification → database validation |
| `testMultiTurnConversation` | Multi-turn conversation with context maintenance (5 turns) |
| `testErrorRecoveryWorkflow` | Tests error recovery: valid query → fallback trigger → recovery |

### 6. Sample API Tests (`SampleApiTest.java`)
| Test Case | Description |
|-----------|-------------|
| `testGetAllPosts` | GET all posts from JSONPlaceholder API |
| `testGetSinglePost` | GET single post by ID |
| `testCreatePost` | POST create new post |
| `testUpdatePost` | PUT update existing post |
| `testDeletePost` | DELETE post by ID |

---

## Test Statistics

| Test Suite | Test Count |
|------------|------------|
| Normal Conversation Tests | 1 |
| Edge Case Tests | 6 |
| Hallucination Tests | 5 |
| Latency Tests | 4 |
| End-to-End Tests | 3 |
| Sample API Tests | 5 |
| **Total** | **24** |

---

## Sample Test Features

The test framework demonstrates:
- REST Assured GET, POST, PUT, DELETE requests
- Jackson JSON serialization/deserialization with annotations
- TestNG annotations and test groups
- Allure annotations for rich reporting
- Log4j 2 logging integration
- Database validation with PostgreSQL
- Latency measurement and thresholds
- Session management
- Fallback and error handling validation

## Useful Maven Commands

```bash
# Clean project
mvn clean

# Compile without running tests
mvn compile

# Skip tests during build
mvn package -DskipTests

# Run tests with verbose output
mvn test -X

# Update project dependencies
mvn versions:display-dependency-updates
```

## Troubleshooting

### Tests not running
- Ensure `testng.xml` references correct test classes
- Check Maven Surefire plugin configuration in pom.xml

### Allure report not generating
- Verify AspectJ weaver is properly configured
- Check that `allure-results` directory is created in target/

### Logging not working
- Verify `log4j2.xml` is in `src/test/resources/`
- Check Log4j dependencies in pom.xml

---

## 📄 License

This project is for educational and testing purposes.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

---

**Built with ❤️ by [The Testing Academy](https://thetestingacademy.com)**
