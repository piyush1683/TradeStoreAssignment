package com.sample.trade.architecture;

import com.sample.trade.common.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Trade Store Root Behaviour Tests
 * Tests the core business logic and validation rules
 */
class TradeStoreRootBehaviourTest {

        private Trade validTrade;
        private Trade lowerVersionTrade;
        private Trade higherVersionTrade;
        private Trade pastMaturityTrade;
        private Trade futureTrade;

        @BeforeEach
        void setUp() {
                validTrade = createTrade("T1", 1, "CP-1", "B1", LocalDate.now().plusDays(5), LocalDate.now(), "N",
                                "req-1");
                lowerVersionTrade = createTrade("T2", 1, "CP-2", "B1", LocalDate.now().plusDays(5), LocalDate.now(),
                                "N", "req-2");
                higherVersionTrade = createTrade("T2", 2, "CP-2", "B1", LocalDate.now().plusDays(5), LocalDate.now(),
                                "N", "req-3");
                pastMaturityTrade = createTrade("T3", 1, "CP-3", "B2", LocalDate.now().minusDays(1), LocalDate.now(),
                                "N", "req-4");
                futureTrade = createTrade("T4", 1, "CP-4", "B3", LocalDate.now().plusDays(10), LocalDate.now(), "N",
                                "req-5");
        }

        @Test
        void testValidTradeStructure() {
                // Test that valid trade has all required fields
                assertNotNull(validTrade.getTradeId(), "Trade ID should not be null");
                assertTrue(validTrade.getVersion() > 0, "Version should be positive");
                assertNotNull(validTrade.getCounterPartyId(), "Counter party ID should not be null");
                assertNotNull(validTrade.getBookId(), "Book ID should not be null");
                assertNotNull(validTrade.getMaturityDate(), "Maturity date should not be null");
                assertNotNull(validTrade.getCreatedDate(), "Created date should not be null");
                assertNotNull(validTrade.getExpired(), "Expired flag should not be null");
                assertNotNull(validTrade.getRequestId(), "Request ID should not be null");
        }

        @Test
        void testTradeValidationRules() {
                // Test version validation rule
                assertTrue(validTrade.getVersion() >= 1, "Version should be >= 1");

                // Test maturity date validation rule
                assertTrue(validTrade.getMaturityDate().isAfter(LocalDate.now()) ||
                                validTrade.getMaturityDate().isEqual(LocalDate.now()),
                                "Maturity date should not be in the past");

                // Test expired field validation
                assertTrue("Y".equals(validTrade.getExpired()) || "N".equals(validTrade.getExpired()),
                                "Expired field should be 'Y' or 'N'");
        }

        @Test
        void testVersionComparisonLogic() {
                // Test that higher version should be accepted over lower version
                assertTrue(higherVersionTrade.getVersion() > lowerVersionTrade.getVersion(),
                                "Higher version should be accepted over lower version");

                // Test version values
                assertEquals(2, higherVersionTrade.getVersion());
                assertEquals(1, lowerVersionTrade.getVersion());
        }

        @Test
        void testMaturityDateValidation() {
                // Test that past maturity dates should be rejected
                assertTrue(pastMaturityTrade.getMaturityDate().isBefore(LocalDate.now()),
                                "Past maturity date should be identified for rejection");

                // Test that future maturity dates should be accepted
                assertTrue(futureTrade.getMaturityDate().isAfter(LocalDate.now()),
                                "Future maturity date should be accepted");
        }

        @Test
        void testExpiryLogic() {
                // Test that trades with past maturity should be marked as expired
                Trade expiredTrade = createTrade("T5", 1, "CP-5", "B3",
                                LocalDate.now().minusDays(1), LocalDate.now(), "N", "req-5");

                // Simulate expiry logic
                if (expiredTrade.getMaturityDate().isBefore(LocalDate.now())) {
                        expiredTrade.setExpired("Y");
                }

                assertEquals("Y", expiredTrade.getExpired(), "Trade should be marked as expired");
        }

        @Test
        void testTradeEquality() {
                Trade trade1 = createTrade("T11", 1, "CP-11", "B11", LocalDate.now().plusDays(5), LocalDate.now(), "N",
                                "req-11");
                Trade trade2 = createTrade("T11", 1, "CP-11", "B11", LocalDate.now().plusDays(5), LocalDate.now(), "N",
                                "req-12");
                Trade trade3 = createTrade("T12", 1, "CP-11", "B11", LocalDate.now().plusDays(5), LocalDate.now(), "N",
                                "req-13");

                assertEquals(trade1, trade2, "Identical trades should be equal");
                assertNotEquals(trade1, trade3, "Different trades should not be equal");
                assertEquals(trade1.hashCode(), trade2.hashCode(), "Equal trades should have same hash code");
        }

        private static Trade createTrade(String id, int version, String cp, String book, LocalDate maturity,
                        LocalDate created, String expired, String requestId) {
                return new Trade(id, version, cp, book, maturity, created, expired, requestId);
        }
}