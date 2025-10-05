package com.sample.trade.common.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class for trade exception records
 */

// Default constructor
public record TradeException(Long id, String tradeId, String requestId, Integer version, String counterPartyId,
        String bookId, LocalDate maturityDate, LocalDate createdDate,
        String expired, String exceptionReason, LocalDateTime createdAt) {
}
