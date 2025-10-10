package com.sample.trade.architecture;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.model.TradeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TradeStoreException extends RuntimeException {
    private final TradeException tradeException;

    public TradeStoreException(TradeException tradeException) {
        super(tradeException.exceptionReason());
        this.tradeException = tradeException;
    }

    public TradeException getTradeException() {
        return tradeException;
    }
}

@DisplayName("Trade Store TDD Test Cases")
class TradeStoreTest {

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
    }

    // First Test Case to accept first trade for new trade ID
    @Test
    @DisplayName("Should accept first trade for new trade ID")
    void shouldAcceptFirstTradeForNewId() {
        // Given: New trade ID T1 with future maturity date
        Trade newTrade = createTrade("T1", 1, "CP-1", "B1",
                today.plusDays(30), today, "N", "req-1");

        // When: Validating first trade
        // Then: Should pass validation without issues
        assertDoesNotThrow(() -> validateTrade(newTrade));

        // Verify trade properties
        assertNotNull(newTrade.getTradeId());
        assertEquals(1, newTrade.getVersion());
        assertEquals("CP-1", newTrade.getCounterPartyId());
    }

    @Test
    @DisplayName("Should reject trade with lower version than existing trade")
    void shouldRejectLowerVersionTrade() {
        // Given: Existing trade with version 2 and new trade with version 1
        Trade existingTrade = createTrade("T2", 2, "CP-2", "B1",
                today.plusDays(30), today, "N", "req-1");
        Trade lowerVersionTrade = createTrade("T2", 1, "CP-1", "B1",
                today.plusDays(30), today, "N", "req-2");

        // When: Validating version comparison
        // Then: Should throw exception for lower version
        TradeStoreException exception = assertThrows(TradeStoreException.class, () -> {
            validateVersionComparison(existingTrade, lowerVersionTrade);
        });

        assertEquals("T2", exception.getTradeException().tradeId());
        assertEquals(1, exception.getTradeException().version());
        assertTrue(exception.getTradeException().exceptionReason().contains("Lower version"));
    }

    @Test
    @DisplayName("Should accept trade with same version (replaces existing)")
    void shouldAcceptSameVersionTrade() {
        // Given: Existing trade with version 2 and new trade with same version
        Trade existingTrade = createTrade("T2", 2, "CP-2", "B1",
                today.plusDays(30), today, "N", "req-1");
        Trade sameVersionTrade = createTrade("T2", 2, "CP-1", "B1",
                today.plusDays(30), today, "N", "req-2");

        // When: Validating same version trade
        // Then: Should accept same version (replacement allowed)
        assertDoesNotThrow(() -> validateVersionComparison(existingTrade, sameVersionTrade));

        // Verify both trades have same version
        assertEquals(existingTrade.getVersion(), sameVersionTrade.getVersion());
        assertEquals("T2", existingTrade.getTradeId());
        assertEquals("T2", sameVersionTrade.getTradeId());
    }

    @Test
    @DisplayName("Should accept trade with higher version")
    void shouldAcceptHigherVersionTrade() {
        // Given: Existing trade with version 1 and new trade with higher version
        Trade existingTrade = createTrade("T2", 1, "CP-1", "B1",
                today.plusDays(30), today, "N", "req-1");
        Trade higherVersionTrade = createTrade("T2", 3, "CP-3", "B1",
                today.plusDays(30), today, "N", "req-2");

        // When: Validating higher version trade
        // Then: Should accept higher version
        assertDoesNotThrow(() -> validateVersionComparison(existingTrade, higherVersionTrade));

        // Verify higher version is accepted
        assertTrue(higherVersionTrade.getVersion() > existingTrade.getVersion());
        assertEquals("T2", higherVersionTrade.getTradeId());
    }

    @Test
    @DisplayName("Should reject trade with maturity date in the past")
    void shouldRejectPastMaturityDateTrade() {
        // Given: Trade with maturity date in the past
        Trade pastMaturityTrade = createTrade("T3", 1, "CP-3", "B2",
                today.minusDays(1), today, "N", "req-1");

        // When: Validating maturity date
        // Then: Should throw exception
        TradeStoreException exception = assertThrows(TradeStoreException.class, () -> {
            validateMaturityDate(pastMaturityTrade);
        });

        assertEquals("T3", exception.getTradeException().tradeId());
        assertTrue(exception.getTradeException().exceptionReason().contains("maturity date"));
    }

    @Test
    @DisplayName("Should accept trade with maturity date today")
    void shouldAcceptTodayMaturityDateTrade() {
        // Given: Trade with maturity date today
        Trade todayMaturityTrade = createTrade("T4", 1, "CP-4", "B3",
                today, today, "N", "req-1");

        // When: Validating maturity date
        // Then: Should accept today's date
        assertDoesNotThrow(() -> validateMaturityDate(todayMaturityTrade));

        // Verify maturity date is today
        assertEquals(today, todayMaturityTrade.getMaturityDate());
    }

    @Test
    @DisplayName("Should accept trade with maturity date in the future")
    void shouldAcceptFutureMaturityDateTrade() {
        // Given: Trade with maturity date in the future
        Trade futureMaturityTrade = createTrade("T5", 1, "CP-5", "B4",
                today.plusDays(30), today, "N", "req-1");

        // When: Validating maturity date
        // Then: Should accept future date
        assertDoesNotThrow(() -> validateMaturityDate(futureMaturityTrade));

        // Verify maturity date is in the future
        assertTrue(futureMaturityTrade.getMaturityDate().isAfter(today));
    }

    @Test
    @DisplayName("Should identify expired trades")
    void shouldIdentifyExpiredTrades() {
        // Given: Trade with past maturity date
        Trade expiredTrade = createTrade("T6", 1, "CP-6", "B5",
                today.minusDays(1), today, "N", "req-1");

        // When: Checking if trade is expired
        // Then: Should identify as expired
        assertTrue(isTradeExpired(expiredTrade));
    }

    @Test
    @DisplayName("Should not identify active trades as expired")
    void shouldNotIdentifyActiveTradesAsExpired() {
        // Given: Trade with future maturity date
        Trade activeTrade = createTrade("T7", 1, "CP-7", "B6",
                today.plusDays(30), today, "N", "req-1");

        // When: Checking if trade is expired
        // Then: Should not be expired
        assertFalse(isTradeExpired(activeTrade));
    }

    @Test
    @DisplayName("Should reject trade with maturity date far in the past")
    void shouldRejectFarPastMaturityDateTrade() {
        // Given: Trade with maturity date far in the past (like T2,1 from problem
        // statement)
        Trade farPastMaturityTrade = createTrade("T2", 1, "CP-1", "B1",
                LocalDate.of(2015, 3, 14), today, "N", "req-1");

        // When: Validating maturity date
        // Then: Should throw exception
        TradeStoreException exception = assertThrows(TradeStoreException.class, () -> {
            validateMaturityDate(farPastMaturityTrade);
        });

        assertEquals("T2", exception.getTradeException().tradeId());
        assertTrue(exception.getTradeException().exceptionReason().contains("maturity date"));
    }

    @Test
    @DisplayName("Should handle null trade gracefully")
    void shouldHandleNullTradeGracefully() {
        // When: Attempting to validate null trade
        // Then: Should throw appropriate exception
        assertThrows(IllegalArgumentException.class, () -> {
            validateTrade(null);
        });
    }

    @Test
    @DisplayName("Should handle trade with null trade ID")
    void shouldHandleTradeWithNullId() {
        // Given: Trade with null ID
        Trade invalidTrade = createTrade(null, 1, "CP-1", "B1",
                today.plusDays(30), today, "N", "req-1");

        // When: Attempting to validate trade
        // Then: Should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            validateTrade(invalidTrade);
        });
    }

    @Test
    @DisplayName("Should handle trade with zero version")
    void shouldHandleTradeWithZeroVersion() {
        // Given: Trade with version 0
        Trade zeroVersionTrade = createTrade("T9", 0, "CP-9", "B8",
                today.plusDays(30), today, "N", "req-1");

        // When: Attempting to validate trade
        // Then: Should throw exception
        TradeStoreException exception = assertThrows(TradeStoreException.class, () -> {
            validateTrade(zeroVersionTrade);
        });

        assertTrue(exception.getTradeException().exceptionReason().contains("version"));
    }

    @Test
    @DisplayName("Should handle trade with negative version")
    void shouldHandleTradeWithNegativeVersion() {
        // Given: Trade with negative version
        Trade negativeVersionTrade = createTrade("T10", -1, "CP-10", "B9",
                today.plusDays(30), today, "N", "req-1");

        // When: Attempting to validate trade
        // Then: Should throw exception
        TradeStoreException exception = assertThrows(TradeStoreException.class, () -> {
            validateTrade(negativeVersionTrade);
        });

        assertTrue(exception.getTradeException().exceptionReason().contains("version"));
    }

    @Test
    @DisplayName("Should handle T1 trade successfully")
    void shouldHandleT1TradeSuccessfully() {
        // Given: T1,1,CP-1,B1,<future date>,<today>,N
        Trade t1Trade = createTrade("T1", 1, "CP-1", "B1",
                today.plusDays(30), today, "N", "req-t1");

        // When: Validating T1 trade
        // Then: Should succeed
        assertDoesNotThrow(() -> validateTrade(t1Trade));

        // Verify trade properties
        assertEquals("T1", t1Trade.getTradeId());
        assertEquals(1, t1Trade.getVersion());
        assertEquals("CP-1", t1Trade.getCounterPartyId());
    }

    @Test
    @DisplayName("Should handle T2 version 2 trade successfully")
    void shouldHandleT2Version2TradeSuccessfully() {
        // Given: T2,2,CP-2,B1,20/05/2026,<today>,N (updated to future date)
        Trade t2v2Trade = createTrade("T2", 2, "CP-2", "B1",
                LocalDate.of(2026, 5, 20), today, "N", "req-t2v2");

        // When: Validating T2 version 2 trade
        // Then: Should succeed
        assertDoesNotThrow(() -> validateTrade(t2v2Trade));

        // Verify trade properties
        assertEquals(2, t2v2Trade.getVersion());
        assertEquals("CP-2", t2v2Trade.getCounterPartyId());
    }

    @Test
    @DisplayName("Should reject T2 version 1 trade (lower version)")
    void shouldRejectT2Version1Trade() {
        // Given: T2,2 already exists, trying to add
        // T2,1,CP-1,B1,20/05/2026,14/03/2025,N (updated dates)
        Trade existingT2v2 = createTrade("T2", 2, "CP-2", "B1",
                LocalDate.of(2026, 5, 20), today, "N", "req-t2v2");
        Trade t2v1Trade = createTrade("T2", 1, "CP-1", "B1",
                LocalDate.of(2026, 5, 20), LocalDate.of(2025, 3, 14), "N", "req-t2v1");

        // When: Validating version comparison
        // Then: Should reject due to lower version
        TradeStoreException exception = assertThrows(TradeStoreException.class, () -> {
            validateVersionComparison(existingT2v2, t2v1Trade);
        });

        assertEquals("T2", exception.getTradeException().tradeId());
        assertEquals(1, exception.getTradeException().version());
        assertTrue(exception.getTradeException().exceptionReason().contains("Lower version"));
    }

    // Helper methods for unit testing validation logic

    private void validateTrade(Trade trade) {
        if (trade == null) {
            throw new IllegalArgumentException("Trade cannot be null");
        }
        if (trade.getTradeId() == null) {
            throw new IllegalArgumentException("Trade ID cannot be null");
        }
        if (trade.getVersion() <= 0) {
            TradeException tradeException = new TradeException(1L, trade.getTradeId(), trade.getRequestId(),
                    trade.getVersion(), trade.getCounterPartyId(), trade.getBookId(),
                    trade.getMaturityDate(), trade.getCreatedDate(), trade.getExpired(),
                    "Invalid version: " + trade.getVersion(), LocalDateTime.now());
            throw new TradeStoreException(tradeException);
        }
        validateMaturityDate(trade);
    }

    private void validateVersionComparison(Trade existingTrade, Trade newTrade) {
        if (newTrade.getVersion() < existingTrade.getVersion()) {
            TradeException tradeException = new TradeException(1L, newTrade.getTradeId(), newTrade.getRequestId(),
                    newTrade.getVersion(), newTrade.getCounterPartyId(), newTrade.getBookId(),
                    newTrade.getMaturityDate(), newTrade.getCreatedDate(), newTrade.getExpired(),
                    "Lower version received: " + newTrade.getVersion() + " < " + existingTrade.getVersion(),
                    LocalDateTime.now());
            throw new TradeStoreException(tradeException);
        }
    }

    private void validateMaturityDate(Trade trade) {
        // Only reject past maturity dates if the trade is not already marked as expired
        if (trade.getMaturityDate().isBefore(today) && !"Y".equals(trade.getExpired())) {
            TradeException tradeException = new TradeException(1L, trade.getTradeId(), trade.getRequestId(),
                    trade.getVersion(), trade.getCounterPartyId(), trade.getBookId(),
                    trade.getMaturityDate(), trade.getCreatedDate(), trade.getExpired(),
                    "Trade maturity date is in the past: " + trade.getMaturityDate(),
                    LocalDateTime.now());
            throw new TradeStoreException(tradeException);
        }
    }

    private boolean isTradeExpired(Trade trade) {
        return trade.getMaturityDate().isBefore(today) && "N".equals(trade.getExpired());
    }

    private Trade createTrade(String id, int version, String counterPartyId, String bookId,
            LocalDate maturityDate, LocalDate createdDate, String expired, String requestId) {
        return new Trade(id, version, counterPartyId, bookId, maturityDate, createdDate, expired, requestId);
    }
}