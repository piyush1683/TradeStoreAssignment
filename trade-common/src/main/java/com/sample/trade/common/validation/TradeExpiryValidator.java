package com.sample.trade.common.validation;

import com.sample.trade.common.model.TradeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

/**
 * Validator implementation for trade expiry validation.
 * 
 * <p>
 * This validator ensures that the trade has not exceeded its maturity date
 * and should not be marked as expired according to business rules.
 * It supports both Trade and TradeRecord objects.
 * </p>
 * 
 * @author piyush.agrawal
 * @version 1.0
 * @since 1.0
 */
public class TradeExpiryValidator implements ConstraintValidator<TradeExpiryValid, Object> {

    private static final Logger logger = LoggerFactory.getLogger(TradeExpiryValidator.class);

    @Override
    public void initialize(TradeExpiryValid constraintAnnotation) {
        String failureMessage = constraintAnnotation.failureMessage();
        logger.debug("Initialized TradeExpiryValidator with failure message: {}", failureMessage);
    }

    @Override
    public boolean isValid(Object tradeObject, ConstraintValidatorContext context) {
        if (tradeObject == null) {
            logger.warn("Cannot check expiry for null trade object");
            return false;
        }

        // Extract common properties from TradeRecord
        String tradeId;
        LocalDate maturityDate;

        if (tradeObject instanceof TradeRecord tradeRecord) {
            tradeId = tradeRecord.getTradeId();
            maturityDate = tradeRecord.getMaturityDate();
        } else {
            logger.warn("Cannot check expiry for unsupported object type: {}",
                    tradeObject.getClass().getSimpleName());
            return false;
        }

        if (maturityDate == null) {
            logger.warn("Cannot check expiry for trade with null maturity date: {}", tradeId);
            return false;
        }

        LocalDate today = LocalDate.now();
        boolean isExpired = maturityDate.isBefore(today);

        if (isExpired) {
            logger.info("Trade {} has expired. Maturity date: {}, Today: {}",
                    tradeId, maturityDate, today);

            // Customize the constraint violation message
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Trade has already expired based on maturity date")
                    .addPropertyNode("maturityDate")
                    .addConstraintViolation();
        } else {
            logger.debug("Trade {} is still active. Maturity date: {}, Today: {}",
                    tradeId, maturityDate, today);
        }

        return !isExpired; // Return true if NOT expired (valid)
    }
}
