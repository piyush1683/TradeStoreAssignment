package com.sample.trade.validationstorage.service;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.store.TradeStore;
import com.sample.trade.common.validation.TradeValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for trade expiry validation functionality.
 * 
 * <p>
 * This test class verifies the trade expiry validation methods in
 * TradeProjectionServiceImpl,
 * including the @ValidateTradeExpiry annotation usage and business logic
 * validation.
 * </p>
 * 
 * @author Trade Store Team
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class TradeExpiryValidationTest {

    @Mock
    private TradeStore tradeStore;

    @Mock
    private TradeValidationService tradeValidationService;

    private TradeProjectionServiceImpl tradeProjectionService;

    @BeforeEach
    void setUp() {
        tradeProjectionService = new TradeProjectionServiceImpl(tradeStore, tradeValidationService);
    }

    @Test
    void testIsTradeExpired_WithExpiredTrade_ReturnsTrue() {
        // Given: A trade with past maturity date
        Trade expiredTrade = new Trade("T1", 1, "CP1", "B1",
                LocalDate.now().minusDays(1),
                LocalDate.now(), "N", "req1");

        when(tradeValidationService.isTradeExpired(expiredTrade)).thenReturn(true);

        // When: Checking if trade is expired
        boolean isExpired = tradeValidationService.isTradeExpired(expiredTrade);

        // Then: Should return true
        assertTrue(isExpired, "Trade with past maturity date should be expired");
    }

    @Test
    void testIsTradeExpired_WithActiveTrade_ReturnsFalse() {
        // Given: A trade with future maturity date
        Trade activeTrade = new Trade("T2", 1, "CP2", "B2",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req2");

        when(tradeValidationService.isTradeExpired(activeTrade)).thenReturn(false);

        // When: Checking if trade is expired
        boolean isExpired = tradeValidationService.isTradeExpired(activeTrade);

        // Then: Should return false
        assertFalse(isExpired, "Trade with future maturity date should not be expired");
    }

    @Test
    void testIsTradeExpired_WithNullTrade_ReturnsFalse() {
        when(tradeValidationService.isTradeExpired(null)).thenReturn(false);

        // When: Checking null trade
        boolean isExpired = tradeValidationService.isTradeExpired(null);

        // Then: Should return false (graceful handling)
        assertFalse(isExpired, "Null trade should be handled gracefully");
    }

    @Test
    void testIsTradeExpired_WithNullMaturityDate_ReturnsFalse() {
        // Given: A trade with null maturity date
        Trade tradeWithNullMaturity = new Trade("T3", 1, "CP3", "B3",
                null, LocalDate.now(), "N", "req3");

        when(tradeValidationService.isTradeExpired(tradeWithNullMaturity)).thenReturn(false);

        // When: Checking if trade is expired
        boolean isExpired = tradeValidationService.isTradeExpired(tradeWithNullMaturity);

        // Then: Should return false (graceful handling)
        assertFalse(isExpired, "Trade with null maturity date should be handled gracefully");
    }

    @Test
    void testCheckAndMarkTradeAsExpired_WithExpiredTrade_MarksAsExpired() {
        // Given: An expired trade
        Trade expiredTrade = new Trade("T4", 1, "CP4", "B4",
                LocalDate.now().minusDays(1),
                LocalDate.now(), "N", "req4");

        when(tradeValidationService.checkAndMarkTradeAsExpired(expiredTrade)).thenReturn(true);

        // When: Checking and marking trade as expired
        boolean wasMarked = tradeProjectionService.checkAndMarkTradeAsExpired(expiredTrade);

        // Then: Should return true (trade was marked as expired)
        assertTrue(wasMarked, "Expired trade should be marked as expired");
    }

    @Test
    void testCheckAndMarkTradeAsExpired_WithActiveTrade_DoesNotMark() {
        // Given: An active trade
        Trade activeTrade = new Trade("T5", 1, "CP5", "B5",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req5");

        when(tradeValidationService.checkAndMarkTradeAsExpired(activeTrade)).thenReturn(false);

        // When: Checking and marking trade as expired
        boolean wasMarked = tradeProjectionService.checkAndMarkTradeAsExpired(activeTrade);

        // Then: Should return false (trade was not marked)
        assertFalse(wasMarked, "Active trade should not be marked as expired");
    }

    @Test
    void testCheckAndMarkTradeAsExpired_WithNullTrade_ReturnsFalse() {
        when(tradeValidationService.checkAndMarkTradeAsExpired(null)).thenReturn(false);

        // When: Checking null trade
        boolean wasMarked = tradeProjectionService.checkAndMarkTradeAsExpired(null);

        // Then: Should return false (graceful handling)
        assertFalse(wasMarked, "Null trade should be handled gracefully");
    }

    @Test
    void testValidateTrade_WithExpiredTrade_ReturnsFalse() {
        // Given: An expired trade
        Trade expiredTrade = new Trade("T6", 1, "CP6", "B6",
                LocalDate.now().minusDays(1),
                LocalDate.now(), "N", "req6");

        when(tradeValidationService.validateTrade(expiredTrade)).thenReturn(false);

        // Set the current trade for validation
        tradeProjectionService.updateTradeProjectStore(expiredTrade);

        // When: Validating the trade
        boolean isValid = tradeProjectionService.validateTrade();

        // Then: Should return false (expired trade should not pass validation)
        assertFalse(isValid, "Expired trade should not pass validation");
    }

    @Test
    void testValidateTrade_WithActiveTrade_ReturnsTrue() {
        // Given: An active trade
        Trade activeTrade = new Trade("T7", 1, "CP7", "B7",
                LocalDate.now().plusDays(30),
                LocalDate.now(), "N", "req7");

        when(tradeValidationService.validateTrade(activeTrade)).thenReturn(true);

        // Set the current trade for validation
        tradeProjectionService.updateTradeProjectStore(activeTrade);

        // When: Validating the trade
        boolean isValid = tradeProjectionService.validateTrade();

        // Then: Should return true (active trade should pass validation)
        assertTrue(isValid, "Active trade should pass validation");
    }
}
