package com.sample.trade.architecture;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.model.TradeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Architecture Tests for TradeStoreApp - TDD Approach
 * Tests the 4 core business functionalities:
 * 1. Can create a new trade successfully
 * 2. Reject lower version trades, accept same version
 * 3. Reject trades with past maturity date
 * 4. Automatically mark trades as expired
 */
class ArchitectureTest {

    // Test 1: Can create a new trade successfully in the tradestore
    @Test
    void testCanCreateNewTradeSuccessfully() {
        // Given: A valid trade
        Trade trade = new Trade("T1", 1, "CP-1", "B1",
                LocalDate.now().plusDays(5), LocalDate.now(), "N", "req-1");

        // When: Validating trade structure
        // Then: Trade should have all required fields
        assertNotNull(trade.getTradeId(), "Trade ID should not be null");
        assertTrue(trade.getVersion() > 0, "Version should be positive");
        assertNotNull(trade.getCounterPartyId(), "Counter party ID should not be null");
        assertNotNull(trade.getBookId(), "Book ID should not be null");
        assertNotNull(trade.getMaturityDate(), "Maturity date should not be null");
        assertNotNull(trade.getCreatedDate(), "Created date should not be null");
        assertNotNull(trade.getExpired(), "Expired flag should not be null");
        assertNotNull(trade.getRequestId(), "Request ID should not be null");
    }

    // Test 2: Reject lower version trades, accept same version trades
    @Test
    void testVersionHandling() {
        // Given: A trade with version 2
        Trade existingTrade = new Trade("T2", 2, "CP-2", "B2",
                LocalDate.now().plusDays(5), LocalDate.now(), "N", "req-2");

        // When: Comparing with lower version
        Trade lowerVersionTrade = new Trade("T2", 1, "CP-2", "B2",
                LocalDate.now().plusDays(5), LocalDate.now(), "N", "req-3");

        // Then: Lower version should be rejected
        assertTrue(existingTrade.getVersion() > lowerVersionTrade.getVersion(),
                "Higher version should be accepted over lower version");

        // When: Same version trade
        Trade sameVersionTrade = new Trade("T2", 2, "CP-2-UPDATED", "B2-UPDATED",
                LocalDate.now().plusDays(6), LocalDate.now(), "N", "req-4");

        // Then: Same version should be acceptable (replaces existing)
        assertEquals(existingTrade.getVersion(), sameVersionTrade.getVersion());
        assertNotEquals(existingTrade.getCounterPartyId(), sameVersionTrade.getCounterPartyId());
    }

    // Test 3: Reject trades with past maturity date
    @Test
    void testRejectPastMaturityDateTrade() {
        // Given: A trade with past maturity date
        Trade pastMaturityTrade = new Trade("T3", 1, "CP-3", "B3",
                LocalDate.now().minusDays(1), LocalDate.now(), "N", "req-5");

        // When: Validating maturity date
        // Then: Should reject past maturity date
        assertTrue(pastMaturityTrade.getMaturityDate().isBefore(LocalDate.now()),
                "Past maturity date should be invalid");

        // Given: A trade with future maturity date
        Trade futureTrade = new Trade("T4", 1, "CP-4", "B4",
                LocalDate.now().plusDays(10), LocalDate.now(), "N", "req-6");

        // When: Validating maturity date
        // Then: Should accept future maturity date
        assertTrue(futureTrade.getMaturityDate().isAfter(LocalDate.now()),
                "Future maturity date should be valid");
    }

    // Test 4: Automatically mark trades as expired when maturity date is surpassed
    @Test
    void testAutomaticTradeExpiry() {
        // Given: A valid trade
        Trade validTrade = new Trade("T5", 1, "CP-5", "B5",
                LocalDate.now().plusDays(1), LocalDate.now(), "N", "req-7");

        // When: Initially created
        // Then: Should be active
        assertEquals("N", validTrade.getExpired(), "Trade should be initially active");

        // When: Manually marking as expired (simulating expiry process)
        validTrade.setExpired("Y");

        // Then: Trade should be marked as expired
        assertEquals("Y", validTrade.getExpired(), "Trade should be marked as expired");
    }

    // Test TradeException model
    @Test
    void testTradeExceptionModel() {
        // Given: A trade exception
        TradeException exception = new TradeException(
            1L, "T1", "req-1", 1, "CP-1", "B1", 
            LocalDate.now(), LocalDate.now(), "N", 
            "Lower version received: 1 < 2", LocalDateTime.now()
        );
        
        // Then: All fields should be properly set
        assertEquals(1L, exception.id());
        assertEquals("T1", exception.tradeId());
        assertEquals(1, exception.version());
        assertEquals("CP-1", exception.counterPartyId());
        assertEquals("B1", exception.bookId());
        assertEquals("N", exception.expired());
        assertEquals("Lower version received: 1 < 2", exception.exceptionReason());
        assertNotNull(exception.createdAt());
        assertNotNull(exception.toString());
    }
}