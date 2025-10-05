package com.sample.trade.common.validation;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.store.TradeStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for unified trade validation functionality.
 * 
 * <p>
 * This test class verifies the unified trade validation methods in
 * TradeValidationService,
 * including both version and expiry validation with annotations.
 * </p>
 * 
 * @author Trade Store Team
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class TradeValidationServiceTest {

    @Mock
    private TradeStore tradeStore;

    private TradeValidationService tradeValidationService;

    @BeforeEach
    void setUp() {
        tradeValidationService = new TradeValidationService(tradeStore);
    }

    // Version Validation Tests

    @Test
    void testIsVersionValid_WithNewTrade_ReturnsTrue() {
        // Given: A new trade with no previous version
        Trade newTrade = new Trade("T1", 1, "CP1", "B1",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req1");

        when(tradeStore.getLatestVersion("T1")).thenReturn(null);

        // When: Validating the trade version
        boolean isValid = tradeValidationService.isVersionValid(newTrade);

        // Then: Should return true
        assertTrue(isValid, "New trade should be valid when no previous version exists");
    }

    @Test
    void testIsVersionValid_WithHigherVersion_ReturnsTrue() {
        // Given: A trade with higher version than existing
        Trade higherVersionTrade = new Trade("T2", 3, "CP2", "B2",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req2");

        when(tradeStore.getLatestVersion("T2")).thenReturn(2);

        // When: Validating the trade version
        boolean isValid = tradeValidationService.isVersionValid(higherVersionTrade);

        // Then: Should return true
        assertTrue(isValid, "Higher version trade should be valid");
    }

    @Test
    void testIsVersionValid_WithLowerVersion_ReturnsFalse() {
        // Given: A trade with lower version than existing
        Trade lowerVersionTrade = new Trade("T3", 1, "CP3", "B3",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req3");

        when(tradeStore.getLatestVersion("T3")).thenReturn(3);

        // When: Validating the trade version
        boolean isValid = tradeValidationService.isVersionValid(lowerVersionTrade);

        // Then: Should return false
        assertFalse(isValid, "Lower version trade should be rejected");
    }

    // Expiry Validation Tests

    @Test
    void testIsTradeExpired_WithExpiredTrade_ReturnsTrue() {
        // Given: A trade with past maturity date
        Trade expiredTrade = new Trade("T4", 1, "CP4", "B4",
                LocalDate.now().minusDays(1),
                LocalDate.now(), "N", "req4");

        // When: Checking if trade is expired
        boolean isExpired = tradeValidationService.isTradeExpired(expiredTrade);

        // Then: Should return true
        assertTrue(isExpired, "Trade with past maturity date should be expired");
    }

    @Test
    void testIsTradeExpired_WithActiveTrade_ReturnsFalse() {
        // Given: A trade with future maturity date
        Trade activeTrade = new Trade("T5", 1, "CP5", "B5",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req5");

        // When: Checking if trade is expired
        boolean isExpired = tradeValidationService.isTradeExpired(activeTrade);

        // Then: Should return false
        assertFalse(isExpired, "Trade with future maturity date should not be expired");
    }

    // Maturity Date Validation Tests

    @Test
    void testIsMaturityDateValid_WithValidDate_ReturnsTrue() {
        // Given: A trade with future maturity date
        Trade validTrade = new Trade("T6", 1, "CP6", "B6",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req6");

        // When: Validating maturity date
        boolean isValid = tradeValidationService.isMaturityDateValid(validTrade);

        // Then: Should return true
        assertTrue(isValid, "Trade with future maturity date should be valid");
    }

    @Test
    void testIsMaturityDateValid_WithPastDate_ReturnsFalse() {
        // Given: A trade with past maturity date
        Trade invalidTrade = new Trade("T7", 1, "CP7", "B7",
                LocalDate.now().minusDays(1),
                LocalDate.now(), "N", "req7");

        // When: Validating maturity date
        boolean isValid = tradeValidationService.isMaturityDateValid(invalidTrade);

        // Then: Should return false
        assertFalse(isValid, "Trade with past maturity date should be invalid");
    }

    // Comprehensive Validation Tests

    @Test
    void testValidateTrade_WithValidTrade_ReturnsTrue() {
        // Given: A valid trade
        Trade validTrade = new Trade("T8", 1, "CP8", "B8",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req8");

        when(tradeStore.getLatestVersion("T8")).thenReturn(null);

        // When: Validating the trade comprehensively
        boolean isValid = tradeValidationService.validateTrade(validTrade);

        // Then: Should return true
        assertTrue(isValid, "Valid trade should pass comprehensive validation");
    }

    @Test
    void testValidateTrade_WithInvalidVersion_ReturnsFalse() {
        // Given: A trade with lower version
        Trade invalidTrade = new Trade("T9", 1, "CP9", "B9",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req9");

        when(tradeStore.getLatestVersion("T9")).thenReturn(3);

        // When: Validating the trade comprehensively
        boolean isValid = tradeValidationService.validateTrade(invalidTrade);

        // Then: Should return false
        assertFalse(isValid, "Trade with lower version should fail validation");
    }

    @Test
    void testValidateTrade_WithPastMaturityDate_ReturnsFalse() {
        // Given: A trade with past maturity date
        Trade invalidTrade = new Trade("T10", 1, "CP10", "B10",
                LocalDate.now().minusDays(1),
                LocalDate.now(), "N", "req10");

        when(tradeStore.getLatestVersion("T10")).thenReturn(null);

        // When: Validating the trade comprehensively
        boolean isValid = tradeValidationService.validateTrade(invalidTrade);

        // Then: Should return false
        assertFalse(isValid, "Trade with past maturity date should fail validation");
    }

    @Test
    void testValidateTrade_WithExpiredTrade_ReturnsFalse() {
        // Given: An expired trade
        Trade expiredTrade = new Trade("T11", 1, "CP11", "B11",
                LocalDate.now().minusDays(1),
                LocalDate.now(), "N", "req11");

        when(tradeStore.getLatestVersion("T11")).thenReturn(null);

        // When: Validating the trade comprehensively
        boolean isValid = tradeValidationService.validateTrade(expiredTrade);

        // Then: Should return false
        assertFalse(isValid, "Expired trade should fail validation");
    }

    // Null Input Tests

    @Test
    void testValidateTrade_WithNullTrade_ReturnsFalse() {
        // When: Validating null trade
        boolean isValid = tradeValidationService.validateTrade(null);

        // Then: Should return false
        assertFalse(isValid, "Null trade should be handled gracefully");
    }

    @Test
    void testIsVersionValid_WithNullTrade_ReturnsFalse() {
        // When: Validating null trade version
        boolean isValid = tradeValidationService.isVersionValid(null);

        // Then: Should return false
        assertFalse(isValid, "Null trade should be handled gracefully");
    }

    @Test
    void testIsTradeExpired_WithNullTrade_ReturnsFalse() {
        // When: Validating null trade expiry
        boolean isExpired = tradeValidationService.isTradeExpired(null);

        // Then: Should return false
        assertFalse(isExpired, "Null trade should be handled gracefully");
    }

    // Version Conflict Tests

    @Test
    void testHasVersionConflict_WithLowerVersion_ReturnsTrue() {
        // Given: A trade with lower version than existing
        Trade lowerVersionTrade = new Trade("T12", 1, "CP12", "B12",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req12");

        when(tradeStore.getLatestVersion("T12")).thenReturn(3);

        // When: Checking for version conflict
        boolean hasConflict = tradeValidationService.hasVersionConflict(lowerVersionTrade);

        // Then: Should return true
        assertTrue(hasConflict, "Lower version should have conflict");
    }

    @Test
    void testHasVersionConflict_WithHigherVersion_ReturnsFalse() {
        // Given: A trade with higher version than existing
        Trade higherVersionTrade = new Trade("T13", 3, "CP13", "B13",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req13");

        when(tradeStore.getLatestVersion("T13")).thenReturn(2);

        // When: Checking for version conflict
        boolean hasConflict = tradeValidationService.hasVersionConflict(higherVersionTrade);

        // Then: Should return false
        assertFalse(hasConflict, "Higher version should not have conflict");
    }

    // Failure Reason Tests

    @Test
    void testGetValidationFailureReason_WithLowerVersion_ReturnsDetailedReason() {
        // Given: A trade with lower version than existing
        Trade lowerVersionTrade = new Trade("T14", 1, "CP14", "B14",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req14");

        when(tradeStore.getLatestVersion("T14")).thenReturn(3);

        // When: Getting failure reason
        String reason = tradeValidationService.getValidationFailureReason(lowerVersionTrade);

        // Then: Should contain detailed reason
        assertNotNull(reason, "Failure reason should not be null");
        assertTrue(reason.contains("Lower version received"), "Should contain lower version message");
        assertTrue(reason.contains("1 < 3"), "Should contain version comparison");
        assertTrue(reason.contains("T14"), "Should contain trade ID");
    }

    @Test
    void testGetValidationFailureReason_WithNullTrade_ReturnsAppropriateMessage() {
        // When: Getting failure reason for null trade
        String reason = tradeValidationService.getValidationFailureReason(null);

        // Then: Should return appropriate message
        assertEquals("No trade provided for validation", reason);
    }

    // Configurable Action Tests

    @Test
    void testIsVersionValidWithAction_WithLowerVersionAndRejectAction_ReturnsFalse() {
        // Given: A trade with lower version and REJECT action
        Trade lowerVersionTrade = new Trade("T15", 1, "CP15", "B15",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req15");

        when(tradeStore.getLatestVersion("T15")).thenReturn(3);

        // When: Validating with REJECT action
        boolean isValid = tradeValidationService.isVersionValidWithAction(
                lowerVersionTrade,
                ValidateTradeVersion.VersionValidationAction.REJECT,
                true);

        // Then: Should return false
        assertFalse(isValid, "Lower version with REJECT action should return false");
    }

    @Test
    void testIsVersionValidWithAction_WithLowerVersionAndAcceptAction_ReturnsTrue() {
        // Given: A trade with lower version and ACCEPT action
        Trade lowerVersionTrade = new Trade("T16", 1, "CP16", "B16",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req16");

        when(tradeStore.getLatestVersion("T16")).thenReturn(3);

        // When: Validating with ACCEPT action
        boolean isValid = tradeValidationService.isVersionValidWithAction(
                lowerVersionTrade,
                ValidateTradeVersion.VersionValidationAction.ACCEPT,
                true);

        // Then: Should return true
        assertTrue(isValid, "Lower version with ACCEPT action should return true");
    }
}
