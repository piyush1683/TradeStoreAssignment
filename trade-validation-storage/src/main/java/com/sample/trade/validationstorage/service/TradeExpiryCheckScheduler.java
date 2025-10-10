package com.sample.trade.validationstorage.service;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.store.TradeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler component for checking and updating expired trades.
 * 
 * This component runs periodically to identify trades that have passed their
 * maturity date and mark them as expired in the database.
 * 
 * @author piyush.agrawal
 */
@Component
public class TradeExpiryCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TradeExpiryCheckScheduler.class);

    @Autowired
    private TradeStore tradeStore;

    @Scheduled(fixedRate = 3000) // run 3 seconds
    public void checkAndUpdateExpiredTrades() {
        try {
            logger.info("Starting scheduled trade expiry check...");

            // Get all active trades
            List<Trade> activeTrades = tradeStore.getTradesToExpire();
            logger.info("Found {} active trades to check for expiry", activeTrades.size());

            LocalDate today = LocalDate.now();
            int expiredCount = 0;

            for (Trade trade : activeTrades) {
                if (trade.getMaturityDate() != null && trade.getMaturityDate().isBefore(today)) {
                    // Trade has expired
                    trade.setExpired("Y");
                    tradeStore.updateTradeExpiry(trade.getTradeId(), trade.getVersion(), trade.getExpired());
                    expiredCount++;

                    logger.info("Marked trade {} as expired (maturity date: {})",
                            trade.getTradeId(), trade.getMaturityDate());
                }
            }

            logger.info("Trade expiry check completed. {} trades marked as expired", expiredCount);

        } catch (Exception e) {
            logger.error("Error during scheduled trade expiry check: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual method to check and update expired trades.
     * 
     * This can be called programmatically if needed.
     */
    public void manualExpiryCheck() {
        logger.info("Manual trade expiry check initiated...");
        checkAndUpdateExpiredTrades();
    }
}
