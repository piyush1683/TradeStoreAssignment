package com.sample.trade.common.validation;

import com.sample.trade.common.model.TradeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

/**
 * Validator implementation for trade maturity date validation.
 * 
 * <p>
 * This validator ensures that the trade maturity date is not in the past
 * and is a valid business date according to trade validation rules.
 * It supports both Trade and TradeRecord objects.
 * </p>
 * 
 * @author piyush.agrawal
 * @version 1.0
 * @since 1.0
 */
public class MaturityDateValidator implements ConstraintValidator<MaturityDateValid, Object> {

    private static final Logger logger = LoggerFactory.getLogger(MaturityDateValidator.class);

    @Override
    public void initialize(MaturityDateValid constraintAnnotation) {
        String failureMessage = constraintAnnotation.failureMessage();
        logger.debug("Initialized MaturityDateValidator with failure message: {}", failureMessage);
    }

    @Override
    public boolean isValid(Object tradeObject, ConstraintValidatorContext context) {
        if (tradeObject == null) {
            logger.warn("Cannot validate maturity date for null trade object");
            return false;
        }

        // Extract common properties from TradeRecord
        String tradeId;
        LocalDate maturityDate;

        if (tradeObject instanceof TradeRecord tradeRecord) {
            tradeId = tradeRecord.getTradeId();
            maturityDate = tradeRecord.getMaturityDate();
        } else {
            logger.warn("Cannot validate maturity date for unsupported object type: {}",
                    tradeObject.getClass().getSimpleName());
            return false;
        }

        if (maturityDate == null) {
            logger.warn("Cannot validate maturity date for trade with null maturity date: {}", tradeId);
            return false;
        }

        LocalDate today = LocalDate.now();
        boolean isValid = !maturityDate.isBefore(today);

        if (!isValid) {
            logger.warn("Trade {} has invalid maturity date. Maturity: {}, Today: {}",
                    tradeId, maturityDate, today);

            // Customize the constraint violation message
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Trade maturity date is in the past and not acceptable")
                    .addPropertyNode("maturityDate")
                    .addConstraintViolation();
        } else {
            logger.debug("Trade {} has valid maturity date. Maturity: {}, Today: {}",
                    tradeId, maturityDate, today);
        }

        return isValid;
    }
}
