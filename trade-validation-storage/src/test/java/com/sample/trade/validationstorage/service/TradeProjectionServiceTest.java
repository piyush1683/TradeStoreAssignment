package com.sample.trade.validationstorage.service;

import com.sample.trade.common.model.Trade;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.fail;

class TradeProjectionServiceTest {
    @Test
    void testTradeProjectionService() {
        Trade t = new Trade();
        t.setTradeId("T1");
        t.setVersion(1);
        t.setCounterPartyId("CP-1");
        t.setBookId("B1");
        t.setMaturityDate(LocalDate.now().plusDays(30));
        t.setCreatedDate(LocalDate.now());
        t.setExpired("N");

        fail("Implement TradeProjectionService tests");
    }
}
