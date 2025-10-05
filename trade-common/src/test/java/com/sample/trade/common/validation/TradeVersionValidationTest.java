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
 * Unit tests for trade version validation functionality.
 * 
 * <p>
 * This test class verifies the trade version validation methods in
 * TradeVersionValidationService,
 * including the @ValidateTradeVersion annotation usage and business logic
 * validation.
 * </p>
 * 
 * @author Trade Store Team
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class TradeVersionValidationTest {

    @Mock
    private TradeStore tradeStore;

    private TradeVersionValidationService versionValidationService;

    @BeforeEach
    void setUp() {
        versionValidationService = new TradeVersionValidationService(tradeStore);
    }

    @Test
    void testIsVersionValid_WithNewTrade_ReturnsTrue() {
        // Given: A new trade with no previous version
        Trade newTrade = new Trade("T1", 1, "CP1", "B1",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req1");

        when(tradeStore.getLatestVersion("T1")).thenReturn(null);

        // When: Validating the trade version
        boolean isValid = versionValidationService.isVersionValid(newTrade);

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
        boolean isValid = versionValidationService.isVersionValid(higherVersionTrade);

        // Then: Should return true
        assertTrue(isValid, "Higher version trade should be valid");
    }

    @Test
    void testIsVersionValid_WithSameVersion_ReturnsTrue() {
        // Given: A trade with same version as existing
        Trade sameVersionTrade = new Trade("T3", 2, "CP3", "B3",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req3");

        when(tradeStore.getLatestVersion("T3")).thenReturn(2);

        // When: Validating the trade version
        boolean isValid = versionValidationService.isVersionValid(sameVersionTrade);

        // Then: Should return true (same version replacement allowed)
        assertTrue(isValid, "Same version trade should be valid for replacement");
    }

    @Test
    void testIsVersionValid_WithLowerVersion_ReturnsFalse() {
        // Given: A trade with lower version than existing
        Trade lowerVersionTrade = new Trade("T4", 1, "CP4", "B4",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req4");

        when(tradeStore.getLatestVersion("T4")).thenReturn(3);

        // When: Validating the trade version
        boolean isValid = versionValidationService.isVersionValid(lowerVersionTrade);

        // Then: Should return false
        assertFalse(isValid, "Lower version trade should be rejected");
    }

    @Test
    void testIsVersionValid_WithNullTrade_ReturnsFalse() {
        // When: Validating null trade
        boolean isValid = versionValidationService.isVersionValid(null);

        // Then: Should return false
        assertFalse(isValid, "Null trade should be handled gracefully");
    }

    @Test
    void testIsVersionValid_WithNullTradeId_ReturnsFalse() {
        // Given: A trade with null trade ID
        Trade tradeWithNullId = new Trade(null, 1, "CP5", "B5",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req5");

        // When: Validating the trade version
        boolean isValid = versionValidationService.isVersionValid(tradeWithNullId);

        // Then: Should return false
        assertFalse(isValid, "Trade with null ID should be handled gracefully");
    }

    @Test
    void testIsVersionValidWithAction_WithLowerVersionAndRejectAction_ReturnsFalse() {
        // Given: A trade with lower version and REJECT action
        Trade lowerVersionTrade = new Trade("T6", 1, "CP6", "B6",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req6");

        when(tradeStore.getLatestVersion("T6")).thenReturn(3);

        // When: Validating with REJECT action
        boolean isValid = versionValidationService.isVersionValidWithAction(
                lowerVersionTrade,
                ValidateTradeVersion.VersionValidationAction.REJECT,
                true);

        // Then: Should return false
        assertFalse(isValid, "Lower version with REJECT action should return false");
    }

    @Test
    void testIsVersionValidWithAction_WithLowerVersionAndAcceptAction_ReturnsTrue() {
        // Given: A trade with lower version and ACCEPT action
        Trade lowerVersionTrade = new Trade("T7", 1, "CP7", "B7",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req7");

        when(tradeStore.getLatestVersion("T7")).thenReturn(3);

        // When: Validating with ACCEPT action
        boolean isValid = versionValidationService.isVersionValidWithAction(
                lowerVersionTrade,
                ValidateTradeVersion.VersionValidationAction.ACCEPT,
                true);

        // Then: Should return true
        assertTrue(isValid, "Lower version with ACCEPT action should return true");
    }

    @Test
    void testHasVersionConflict_WithLowerVersion_ReturnsTrue() {
        // Given: A trade with lower version than existing
        Trade lowerVersionTrade = new Trade("T8", 1, "CP8", "B8",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req8");

        when(tradeStore.getLatestVersion("T8")).thenReturn(3);

        // When: Checking for version conflict
        boolean hasConflict = versionValidationService.hasVersionConflict(lowerVersionTrade);

        // Then: Should return true
        assertTrue(hasConflict, "Lower version should have conflict");
    }

    @Test
    void testHasVersionConflict_WithHigherVersion_ReturnsFalse() {
        // Given: A trade with higher version than existing
        Trade higherVersionTrade = new Trade("T9", 3, "CP9", "B9",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req9");

        when(tradeStore.getLatestVersion("T9")).thenReturn(2);

        // When: Checking for version conflict
        boolean hasConflict = versionValidationService.hasVersionConflict(higherVersionTrade);

        // Then: Should return false
        assertFalse(hasConflict, "Higher version should not have conflict");
    }

    @Test
    void testGetVersionValidationFailureReason_WithLowerVersion_ReturnsDetailedReason() {
        // Given: A trade with lower version than existing
        Trade lowerVersionTrade = new Trade("T10", 1, "CP10", "B10",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req10");

        when(tradeStore.getLatestVersion("T10")).thenReturn(3);

        // When: Getting failure reason
        String reason = versionValidationService.getVersionValidationFailureReason(lowerVersionTrade);

        // Then: Should contain detailed reason
        assertNotNull(reason, "Failure reason should not be null");
        assertTrue(reason.contains("Lower version received"), "Should contain lower version message");
        assertTrue(reason.contains("1 < 3"), "Should contain version comparison");
        assertTrue(reason.contains("T10"), "Should contain trade ID");
    }

    @Test
    void testGetVersionValidationFailureReason_WithNullTrade_ReturnsAppropriateMessage() {
        // When: Getting failure reason for null trade
        String reason = versionValidationService.getVersionValidationFailureReason(null);

        // Then: Should return appropriate message
        assertEquals("No trade provided for version validation", reason);
    }
}
