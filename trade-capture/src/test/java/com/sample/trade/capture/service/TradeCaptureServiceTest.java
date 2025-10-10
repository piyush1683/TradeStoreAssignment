package com.sample.trade.capture.service;

import com.sample.trade.common.model.Trade;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.fail;

class TradeCaptureServiceTest {
    @Test
    void testTradeCaptureService() {
        Trade t = new Trade();
        t.setTradeId("T2");
        t.setVersion(2);
        t.setCounterPartyId("CP-2");
        t.setBookId("B1");
        t.setMaturityDate(LocalDate.now().plusDays(2));
        t.setCreatedDate(LocalDate.now());
        t.setExpired("N");

        fail("Implement TradeCaptureService tests");
    }
}
