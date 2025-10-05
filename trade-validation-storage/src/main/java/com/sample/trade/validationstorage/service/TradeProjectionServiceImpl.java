package com.sample.trade.validationstorage.service;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.store.TradeStore;
import com.sample.trade.common.validation.TradeValidationService;
import com.sample.trade.validationstorage.exception.TradeProjectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service implementation for trade projection management and validation.
 * 
 * <p>
 * This service handles the core business logic for trade validation and
 * projection updates.
 * It implements the validation rules for trade processing including version
 * control,
 * maturity date validation, and expiry checking.
 * </p>
 * 
 * <p>
 * <strong>Key Responsibilities:</strong>
 * </p>
 * <ul>
 * <li>Validate trades against business rules before processing</li>
 * <li>Update trade projection store with validated trades</li>
 * <li>Handle trade exceptions for rejected trades</li>
 * <li>Provide expiry validation functionality</li>
 * </ul>
 * 
 * <p>
 * <strong>Validation Rules:</strong>
 * </p>
 * <ul>
 * <li>Version Control: Reject trades with lower version numbers</li>
 * <li>Maturity Date: Reject trades with past maturity dates</li>
 * <li>Expiry Check: Validate if trade has exceeded its maturity date</li>
 * </ul>
 * 
 * @author Trade Store Team
 * @version 1.0
 * @since 1.0
 */
@Service
public class TradeProjectionServiceImpl implements TradeProjectionService {
    private static final Logger logger = LoggerFactory.getLogger(TradeProjectionServiceImpl.class);

    private final TradeStore tradeStore;
    private final TradeValidationService tradeValidationService;
    private Trade currentTrade;

    public TradeProjectionServiceImpl(TradeStore tradeStore, TradeValidationService tradeValidationService) {
        this.tradeStore = tradeStore;
        this.tradeValidationService = tradeValidationService;
    }

    @Override
    public Trade readTradeEventStore() {
        return null;
    }

    /**
     * Updates the trade projection store with the provided trade.
     * 
     * <p>
     * This method validates the trade against business rules before processing.
     * If validation passes, the trade is inserted/updated in the projection table.
     * If validation fails, the trade is stored in the exception table with the
     * reason.
     * </p>
     * 
     * @param trade the trade to be processed and stored
     * @throws RuntimeException if there's an error during the update process
     */
    @Override
    public void updateTradeProjectStore(Trade trade) {
        if (trade == null) {
            logger.warn("No trade to update");
            return;
        }

        this.currentTrade = trade;

        try {
            // Validate trade first
            if (validateTrade()) {
                // Insert/Update in trade projection table (PostgreSQL)
                tradeStore.insertTrade(trade);
                logger.info("Successfully updated trade projection store for trade: {}", trade.getTradeId());
            } else {
                // Insert into trade exception table
                String exceptionReason = getValidationFailureReason();
                tradeStore.insertTradeException(trade, exceptionReason);
                logger.warn("Trade validation failed, stored in exception table: {}", trade.getTradeId());
            }
        } catch (Exception e) {
            logger.error("Error updating trade projection store: {}", e.getMessage(), e);
            throw new TradeProjectionException("Failed to update trade projection store", e);
        }
    }

    /**
     * Validates the current trade against business rules.
     * 
     * <p>
     * This method performs comprehensive validation including:
     * </p>
     * <ul>
     * <li>Version control validation</li>
     * <li>Maturity date validation</li>
     * <li>Expiry validation</li>
     * </ul>
     * 
     * @return true if the trade passes all validation rules, false otherwise
     */
    @Override
    public boolean validateTrade() {
        if (currentTrade == null) {
            logger.warn("Cannot validate null trade");
            return false;
        }

        try {
            // Use the unified validation service for comprehensive validation
            if (!tradeValidationService.validateTrade(currentTrade)) {
                logger.warn("Rejecting trade: Comprehensive validation failed. Trade: {}, Reason: {}",
                        currentTrade.getTradeId(), tradeValidationService.getValidationFailureReason(currentTrade));
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("Error validating trade: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a trade should be marked as expired and updates its status.
     * 
     * <p>
     * This method delegates to the unified validation service for expiry checking
     * and database updates.
     * </p>
     * 
     * @param trade the trade to check and potentially mark as expired
     * @return true if the trade was marked as expired, false if it's still active
     */
    public boolean checkAndMarkTradeAsExpired(Trade trade) {
        return tradeValidationService.checkAndMarkTradeAsExpired(trade);
    }

    /**
     * Determines the specific reason for trade validation failure.
     * 
     * <p>
     * This method analyzes the current trade and identifies the specific validation
     * rule
     * that caused the failure. It provides detailed error messages for debugging
     * and
     * exception handling purposes.
     * </p>
     * 
     * <p>
     * <strong>Validation Failure Reasons:</strong>
     * </p>
     * <ul>
     * <li>Lower version received: When trade version is less than the latest
     * version</li>
     * <li>Maturity date in past: When trade maturity date is before today</li>
     * <li>Trade expired: When trade has already exceeded its maturity date</li>
     * <li>Validation error: When an exception occurs during validation</li>
     * </ul>
     * 
     * @return detailed reason for validation failure
     */
    private String getValidationFailureReason() {
        if (currentTrade == null) {
            return "No trade data provided for validation";
        }

        try {
            // Use the unified validation service for failure reason
            return tradeValidationService.getValidationFailureReason(currentTrade);
        } catch (Exception e) {
            logger.error("Error determining validation failure reason: {}", e.getMessage());
            return "Validation error: " + e.getMessage();
        }
    }
}
