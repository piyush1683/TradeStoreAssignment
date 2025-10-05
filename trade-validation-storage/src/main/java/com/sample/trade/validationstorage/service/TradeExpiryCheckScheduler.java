package com.sample.trade.validationstorage.service;

import com.sample.trade.common.store.TradeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TradeExpiryCheckScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TradeExpiryCheckScheduler.class);

    @Autowired
    private TradeStore tradeStore;

    @Scheduled(fixedRate = 3000) // Run every 5 minutes
    public void checkExpiry() {
        try {
            logger.info("Starting trade expiry check at: {}", java.time.LocalDateTime.now());

            // Get all active trades
            List<String> activeTradeIds = tradeStore.getActiveTradeIds();

            if (activeTradeIds.isEmpty()) {
                logger.info("No active trades found for expiry check");
                return;
            }

            int expiredCount = 0;
            LocalDate today = LocalDate.now();

            for (String tradeId : activeTradeIds) {
                try {
                    // Get trade details
                    var trade = tradeStore.getTradeById(tradeId);
                    if (trade != null && trade.getMaturityDate().isBefore(today)) {
                        // Mark trade as expired
                        tradeStore.markTradeAsExpired(tradeId);
                        expiredCount++;
                        logger.info("Marked trade as expired: {} (Maturity: {})", tradeId, trade.getMaturityDate());
                    }
                } catch (Exception e) {
                    logger.error("Error checking expiry for trade {}: {}", tradeId, e.getMessage());
                }
            }

            logger.info("Expiry check completed. Expired trades: {}", expiredCount);

        } catch (Exception e) {
            logger.error("Error during trade expiry check: {}", e.getMessage(), e);
        }
    }
}
