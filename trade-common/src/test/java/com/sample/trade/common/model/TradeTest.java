package com.sample.trade.common.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class TradeTest {

    @Test
    void testTradeCreation() {
        Trade trade = new Trade();
        trade.setTradeId("T1");
        trade.setVersion(1);
        trade.setCounterPartyId("CP1");
        trade.setBookId("B1");
        trade.setMaturityDate(LocalDate.now().plusDays(30));
        trade.setCreatedDate(LocalDate.now());
        trade.setExpired("N");
        trade.setRequestId("REQ1");

        assertEquals("T1", trade.getTradeId());
        assertEquals(1, trade.getVersion());
        assertEquals("CP1", trade.getCounterPartyId());
        assertEquals("B1", trade.getBookId());
        assertEquals("N", trade.getExpired());
        assertEquals("REQ1", trade.getRequestId());
    }
}
