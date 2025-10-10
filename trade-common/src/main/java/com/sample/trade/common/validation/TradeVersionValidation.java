package com.sample.trade.common.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.store.TradeStore;

public class TradeVersionValidation {
    private static final Logger logger = LoggerFactory.getLogger(TradeVersionValidation.class);

    private final TradeStore tradeStore;

    public TradeVersionValidation(TradeStore tradeStore) {
        this.tradeStore = tradeStore;
    }

    /**
     * Validates trade version against database to ensure no lower version is
     * accepted.
     * 
     * @param trade the trade to validate
     * @throws ConstraintViolationException if version validation fails
     */
    public void validateTradeVersion(Trade trade) {
        String tradeId = trade.getTradeId();
        int version = trade.getVersion();

        try {
            // Get the latest version from database
            Integer latestVersion = tradeStore.getLatestVersion(tradeId);

            if (latestVersion != null && version < latestVersion) {
                String errorMessage = String.format(
                        "Trade version validation failed: Lower version received (Current: %d, Latest: %d)",
                        version, latestVersion);

                // Create a constraint violation manually and throw exception
                try {
                    tradeStore.insertTradeException(trade, errorMessage);
                    logger.info("Successfully saved validation failure for trade {}: {}",
                            trade.getTradeId(), errorMessage);
                } catch (Exception ex) {
                    logger.error("Error saving validation failure for trade {}: {}",
                            trade.getTradeId(), ex.getMessage(), ex);
                }
            }

            logger.debug("Version validation passed. Trade: {}, Version: {}, Latest: {}",
                    tradeId, version, latestVersion);

        } catch (Exception e) {
            logger.error("Error validating trade version for trade {}: {}",
                    tradeId, e.getMessage(), e);
        }
    }

}
