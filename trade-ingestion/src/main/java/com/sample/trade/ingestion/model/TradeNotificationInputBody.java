package com.sample.trade.ingestion.model;

import java.time.LocalDate;

public record TradeNotificationInputBody(
        String requestId,
        String tradeId,
        LocalDate maturityDate,
        LocalDate createdDate) {

}
