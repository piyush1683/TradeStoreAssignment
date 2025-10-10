package com.sample.trade.validationstorage.service;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.model.TradeRecord;
import com.sample.trade.common.store.TradeStore;
import com.sample.trade.common.validation.TradeVersionValidation;
import com.sample.trade.validationstorage.exception.TradeProjectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;

@Service
public class TradeProjectionServiceImpl implements TradeProjectionService {
    private static final Logger logger = LoggerFactory.getLogger(TradeProjectionServiceImpl.class);

    private final TradeStore tradeStore;
    private final Validator validator;

    public TradeProjectionServiceImpl(TradeStore tradeStore, Validator validator) {
        this.tradeStore = tradeStore;
        this.validator = validator;
    }

    @Override
    public Trade readTradeEventStore() {
        return null;
    }

    @Override
    public void updateTradeProjectStore(Trade trade) {
        if (trade == null) {
            return;
        }

        try {
            // Validate trade record using custom validation method
            validateTrade(trade);

            // Validation passed - insert/update in trade projection table (PostgreSQL)
            tradeStore.insertTrade(trade);
            logger.info("Successfully updated trade projection store for trade: {}", trade.getTradeId());

        } catch (ConstraintViolationException e) {
            // Handle validation failures (including version validation)
            logger.error("Trade validation failed for trade {}: {}", trade.getTradeId(), e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating trade projection store: {}", e.getMessage(), e);
            throw new TradeProjectionException("Failed to update trade projection store", e);
        }
    }

    /**
     * Validates a trade record using the custom validation annotations.
     * 
     * @param tradeRecord the trade record to validate
     * @throws ConstraintViolationException if validation fails
     */
    @Override
    public void validateTrade(Trade trade) {
        TradeRecord tradeRecord = convertToTradeRecord(trade);

        // First, perform database version validation
        TradeVersionValidation tradeVersionValidation = new TradeVersionValidation(tradeStore);
        tradeVersionValidation.validateTradeVersion(trade);

        // Then apply other validation annotations
        Set<ConstraintViolation<TradeRecord>> violations = validator.validate(tradeRecord);

        if (!violations.isEmpty()) {
            handleValidationFailures(tradeRecord, violations);
        }
    }

    /**
     * Handles validation failures by extracting failure messages and saving them to
     * trade_exception table.
     * 
     * @param tradeRecord the trade record that failed validation
     * @param violations  the set of constraint violations
     */
    private void handleValidationFailures(TradeRecord tradeRecord,
            Set<ConstraintViolation<TradeRecord>> violations) {
        StringBuilder failureMessages = new StringBuilder();

        for (ConstraintViolation<TradeRecord> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();

            if (!failureMessages.isEmpty()) {
                failureMessages.append("; ");
            }
            failureMessages.append(propertyPath).append(": ").append(message);
        }

        String combinedFailureMessage = failureMessages.toString();

        Trade trade = convertToTrade(tradeRecord);

        // Save the validation failure to the trade_exception table
        try {
            tradeStore.insertTradeException(trade, combinedFailureMessage);
            logger.info("Successfully saved validation failure for trade {}: {}",
                    trade.getTradeId(), combinedFailureMessage);
        } catch (Exception e) {
            logger.error("Error saving validation failure for trade {}: {}",
                    trade.getTradeId(), e.getMessage(), e);
        }
    }

    /**
     * Converts a Trade to a TradeRecord object for validation.
     * 
     * @param trade the trade to convert
     * @return the converted TradeRecord object
     */
    private TradeRecord convertToTradeRecord(Trade trade) {
        return new TradeRecord(
                trade.getTradeId(),
                trade.getVersion(),
                trade.getCounterPartyId(),
                trade.getBookId(),
                trade.getMaturityDate(),
                trade.getCreatedDate(),
                trade.getExpired(),
                trade.getRequestId());
    }

    /**
     * Converts a TradeRecord to a Trade object for database operations.
     * 
     * @param tradeRecord the trade record to convert
     * @return the converted Trade object
     */
    private Trade convertToTrade(TradeRecord tradeRecord) {
        return new Trade(
                tradeRecord.getTradeId(),
                tradeRecord.getVersion(),
                tradeRecord.getCounterPartyId(),
                tradeRecord.getBookId(),
                tradeRecord.getMaturityDate(),
                tradeRecord.getCreatedDate(),
                tradeRecord.getExpired(),
                tradeRecord.getRequestId());
    }
}