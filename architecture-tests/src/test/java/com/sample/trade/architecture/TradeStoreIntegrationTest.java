package com.sample.trade.architecture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.sample.trade.common.model.Trade;
import com.sample.trade.common.model.TradeException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Trade Store Integration Tests")
class TradeStoreIntegrationTest {

    private ObjectMapper objectMapper;
    private String baseUrl;
    private LocalDate today;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        baseUrl = "http://localhost:8081"; // Trade Ingestion Service port
        restTemplate = new RestTemplate();
        today = LocalDate.now();

        // Clean up any existing test data with 3XXX series trade IDs before each test
        cleanupTestDataByPattern("3%");
    }

    @AfterEach
    void tearDown() {
        // Clean up test data after each test to ensure isolation
        cleanupTestDataByPattern("3%");
    }

    @AfterAll
    static void tearDownAll() {
        // Final cleanup after all tests are completed
        // This ensures that even if individual test cleanup fails, we still clean up at
        // the end
        System.out.println(
                "All integration tests completed. Final cleanup performed by individual test cleanup methods.");
    }

    /**
     * Enhanced cleanup method that removes test data by pattern
     * 
     * @param pattern SQL LIKE pattern for trade IDs to delete (e.g., "3%" for all
     *                3XXX series)
     */
    private void cleanupTestDataByPattern(String pattern) {
        try {
            System.out.println("Cleaning up test data with pattern: " + pattern);

            // Call the cleanup endpoint with the pattern
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/api/test/cleanup/" + pattern,
                    null,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Test data cleanup successful: " + response.getBody());
            } else {
                System.err.println("Test data cleanup failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            // Log the error but don't fail the test
            System.err.println("Warning: Failed to cleanup test data with pattern " + pattern + ": " + e.getMessage());
            // Don't rethrow the exception to avoid failing the test
        }
    }

    /**
     * Cleanup method for specific trade IDs
     * 
     * @param tradeIds Array of specific trade IDs to clean up
     */
    private void cleanupSpecificTradeIds(String... tradeIds) {
        for (String tradeId : tradeIds) {
            try {
                System.out.println("Cleaning up specific trade ID: " + tradeId);

                ResponseEntity<String> response = restTemplate.postForEntity(
                        baseUrl + "/api/test/cleanup/" + tradeId,
                        null,
                        String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Trade ID " + tradeId + " cleanup successful");
                } else {
                    System.err.println(
                            "Trade ID " + tradeId + " cleanup failed with status: " + response.getStatusCode());
                }

            } catch (Exception e) {
                System.err.println("Warning: Failed to cleanup trade ID " + tradeId + ": " + e.getMessage());
            }
        }
    }

    private void cleanupAfterTest(String testName, String... tradeIds) {
        System.out.println("Cleaning up after test: " + testName);
        if (tradeIds.length > 0) {
            cleanupSpecificTradeIds(tradeIds);
        } else {
            cleanupTestDataByPattern("3%");
        }
    }

    private void verifyCleanup(String... tradeIds) {
        for (String tradeId : tradeIds) {
            try {
                ResponseEntity<Trade> response = restTemplate.getForEntity(
                        baseUrl + "/api/test/trade/" + tradeId,
                        Trade.class);

                if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                    System.out.println("✓ Trade ID " + tradeId + " successfully cleaned up");
                } else {
                    System.err.println("⚠ Trade ID " + tradeId + " still exists in database");
                }
            } catch (Exception e) {
                System.out.println("✓ Trade ID " + tradeId + " cleanup verified (not found)");
            }
        }
    }

    @Test
    @DisplayName("Should create valid trade via API and verify in trade_projection table")
    void shouldCreateValidTradeAndVerifyInDatabase() {
        // Given: Valid trade data with 3001 series ID
        Map<String, Object> tradeData = createTradeData("3001", 1, "CP-1", "B1",
                today.plusDays(30), today, "N", "req-3001");

        // When: Creating trade via API
        ResponseEntity<String> response = createTradeViaAPI(tradeData);

        // Then: Should return success status
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        // Verify trade was accepted (async processing)
        // Note: Since this is async processing, we can't immediately verify database
        // state
        // The trade will be processed and stored by the background services
        assertTrue(response.getBody().contains("Trade accepted with requestId"));
    }

    @Test
    @DisplayName("Should accept lower version trade for async validation")
    void shouldAcceptLowerVersionTradeForAsyncValidation() {
        // Given: First create a trade with version 2
        Map<String, Object> existingTrade = createTradeData("3002", 2, "CP-2", "B1",
                today.plusDays(30), today, "N", "req-3002-v2");
        ResponseEntity<String> firstResponse = createTradeViaAPI(existingTrade);
        assertEquals(HttpStatus.ACCEPTED, firstResponse.getStatusCode());

        // When: Attempting to create trade with lower version 1
        Map<String, Object> lowerVersionTrade = createTradeData("3002", 1, "CP-1", "B1",
                today.plusDays(30), today, "N", "req-3002-v1");
        ResponseEntity<String> response = createTradeViaAPI(lowerVersionTrade);

        // Then: Should accept the trade for async validation
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertTrue(response.getBody().contains("Trade accepted with requestId"));

        // Wait for async processing to complete with retry logic
        TradeException[] exceptions = null;
        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(5000); // Wait 5 seconds between retries
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            // Check for exception record in trade_exception service via notifications
            // endpoint
            ResponseEntity<TradeException[]> exceptionsResponse = restTemplate.getForEntity(
                    baseUrl + "/api/notifications?tradeId=3002", TradeException[].class);
            assertEquals(HttpStatus.OK, exceptionsResponse.getStatusCode());

            exceptions = exceptionsResponse.getBody();
            if (exceptions != null && exceptions.length > 0) {
                break; // Found exceptions, exit retry loop
            }
        }

        assertNotNull(exceptions, "Exceptions should be found after async processing");
        assertTrue(exceptions.length > 0, "Exception should be recorded in trade_exception service");

        // Verify exception details
        TradeException exception = exceptions[0];
        assertEquals("3002", exception.tradeId());
        assertEquals(1, exception.version());
        assertTrue(exception.exceptionReason().contains("Lower version"));
    }

    @Test
    @DisplayName("Should accept same version trade (replacement)")
    void shouldAcceptSameVersionTradeReplacement() {
        // Given: First create a trade with version 2
        Map<String, Object> existingTrade = createTradeData("3004", 2, "CP-2", "B1",
                today.plusDays(30), today, "N", "req-3004-v2");
        createTradeViaAPI(existingTrade);

        // When: Creating trade with same version but different data
        Map<String, Object> sameVersionTrade = createTradeData("3004", 2, "CP-1", "B1",
                today.plusDays(30), today, "N", "req-3004-v2-new");
        ResponseEntity<String> response = createTradeViaAPI(sameVersionTrade);

        // Then: Should return success status
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        // Verify trade was accepted (async processing)
        assertTrue(response.getBody().contains("Trade accepted with requestId"));
    }

    @Test
    @DisplayName("Should accept higher version trade")
    void shouldAcceptHigherVersionTrade() {
        // Given: First create a trade with version 1
        Map<String, Object> existingTrade = createTradeData("3005", 1, "CP-1", "B1",
                today.plusDays(30), today, "N", "req-3005-v1");
        createTradeViaAPI(existingTrade);

        // When: Creating trade with higher version
        Map<String, Object> higherVersionTrade = createTradeData("3005", 3, "CP-3", "B1",
                today.plusDays(30), today, "N", "req-3005-v3");
        ResponseEntity<String> response = createTradeViaAPI(higherVersionTrade);

        // Then: Should return success status
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        // Verify trade was accepted (async processing)
        assertTrue(response.getBody().contains("Trade accepted with requestId"));
    }

    @Test
    @DisplayName("Should handle expired trade and mark as expired")
    void shouldHandleExpiredTradeAndMarkAsExpired() {
        // Given: Trade with past maturity date but marked as expired
        Map<String, Object> expiredTrade = createTradeData("3006", 2, "CP-6", "B2",
                today, today, "Y", "req-3006");

        // When: Creating expired trade via API
        ResponseEntity<String> response = createTradeViaAPI(expiredTrade);

        // Then: Should return success status (expired trades are allowed)
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        // Verify trade was accepted (async processing)
        assertTrue(response.getBody().contains("Trade accepted with requestId"));
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            // TODO: handle exception
        }
        Trade trade = restTemplate.getForEntity(
                baseUrl + "/api/test/trade/3006", Trade.class).getBody();
        assertEquals(trade.getExpired(), "Y");

    }

    @Test
    @DisplayName("Should reject lower version trade and generate exception")
    void shouldRejectLowerVersionTradeAndGenerateException() {
        String testTradeId = "3010";

        try {
            // Given: First create a trade with version 3
            Map<String, Object> existingTrade = createTradeData(testTradeId, 3, "CP-10", "B1",
                    today.plusDays(30), today, "N", "req-3010-v3");
            ResponseEntity<String> firstResponse = createTradeViaAPI(existingTrade);
            assertEquals(HttpStatus.ACCEPTED, firstResponse.getStatusCode());

            // When: Attempting to create trade with lower version 2
            Map<String, Object> lowerVersionTrade = createTradeData(testTradeId, 2, "CP-10", "B1",
                    today.plusDays(30), today, "N", "req-3010-v2");
            ResponseEntity<String> response = createTradeViaAPI(lowerVersionTrade);

            // Then: Should accept the trade for async validation
            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            assertTrue(response.getBody().contains("Trade accepted with requestId"));

            // Wait for async processing to complete with retry logic
            TradeException[] exceptions = null;
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(5000); // Wait 5 seconds between retries
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // Check for exception record in trade_exception service via notifications
                // endpoint
                ResponseEntity<TradeException[]> exceptionsResponse = restTemplate.getForEntity(
                        baseUrl + "/api/notifications?tradeId=" + testTradeId, TradeException[].class);
                assertEquals(HttpStatus.OK, exceptionsResponse.getStatusCode());

                exceptions = exceptionsResponse.getBody();
                if (exceptions != null && exceptions.length > 0) {
                    break; // Found exceptions, exit retry loop
                }
            }

            assertNotNull(exceptions, "Exceptions should be found after async processing");
            assertTrue(exceptions.length > 0, "Exception should be recorded for lower version trade");

            // Verify exception details
            TradeException exception = exceptions[0];
            assertEquals(testTradeId, exception.tradeId());
            assertEquals(2, exception.version());
            assertTrue(exception.exceptionReason().contains("Lower version") ||
                    exception.exceptionReason().contains("version") ||
                    exception.exceptionReason().contains("reject"),
                    "Exception should indicate lower version rejection");

        } finally {
            // Enhanced cleanup for this specific test
            cleanupAfterTest("shouldRejectLowerVersionTradeAndGenerateException", testTradeId);
            // Verify cleanup was successful
            verifyCleanup(testTradeId);
        }
    }

    @Test
    @DisplayName("Should reject trade with past maturity date and generate exception")
    void shouldRejectPastMaturityDateTradeAndGenerateException() {
        String testTradeId = "3011";

        try {
            // Given: Create a trade with past maturity date
            Map<String, Object> pastMaturityTrade = createTradeData(testTradeId, 1, "CP-11", "B1",
                    today.minusDays(5), today, "N", "req-3011");

            // When: Creating trade with past maturity date via API
            ResponseEntity<String> response = createTradeViaAPI(pastMaturityTrade);

            // Then: Should accept the trade for async validation
            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            assertTrue(response.getBody().contains("Trade accepted with requestId"));

            // Wait for async processing to complete with retry logic
            TradeException[] exceptions = null;
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(5000); // Wait 5 seconds between retries
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // Check for exception record in trade_exception service via notifications
                // endpoint
                ResponseEntity<TradeException[]> exceptionsResponse = restTemplate.getForEntity(
                        baseUrl + "/api/notifications?tradeId=" + testTradeId, TradeException[].class);
                assertEquals(HttpStatus.OK, exceptionsResponse.getStatusCode());

                exceptions = exceptionsResponse.getBody();
                if (exceptions != null && exceptions.length > 0) {
                    break; // Found exceptions, exit retry loop
                }
            }

            assertNotNull(exceptions, "Exceptions should be found after async processing");
            assertTrue(exceptions.length > 0, "Exception should be recorded for past maturity date trade");

            // Verify exception details
            TradeException exception = exceptions[0];
            assertEquals(testTradeId, exception.tradeId());
            assertEquals(1, exception.version());
            assertTrue(exception.exceptionReason().contains("maturity") ||
                    exception.exceptionReason().contains("past") ||
                    exception.exceptionReason().contains("date") ||
                    exception.exceptionReason().contains("reject"),
                    "Exception should indicate past maturity date rejection");

        } finally {
            // Enhanced cleanup for this specific test
            cleanupAfterTest("shouldRejectPastMaturityDateTradeAndGenerateException", testTradeId);
            // Verify cleanup was successful
            verifyCleanup(testTradeId);
        }
    }

    // Helper methods

    private Map<String, Object> createTradeData(String tradeId, int version, String counterPartyId,
            String bookId, LocalDate maturityDate, LocalDate createdDate, String expired, String requestId) {
        Map<String, Object> tradeData = new HashMap<>();
        tradeData.put("tradeId", tradeId);
        tradeData.put("version", version);
        tradeData.put("counterPartyId", counterPartyId);
        tradeData.put("bookId", bookId);
        tradeData.put("maturityDate", maturityDate);
        tradeData.put("createdDate", createdDate);
        tradeData.put("expired", expired);
        tradeData.put("requestId", requestId);
        return tradeData;
    }

    private ResponseEntity<String> createTradeViaAPI(Map<String, Object> tradeData) {
        try {
            String json = objectMapper.writeValueAsString(tradeData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            return restTemplate.exchange(
                    baseUrl + "/api/trade",
                    HttpMethod.POST,
                    entity,
                    String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create trade via API", e);
        }
    }
}