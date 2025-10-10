package com.sample.trade.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for validating trade expiry status.
 * 
 * <p>
 * This annotation ensures that the trade has not exceeded its maturity date
 * and should not be marked as expired according to business rules.
 * </p>
 * 
 * @author piyush.agrawal
 * @version 1.0
 * @since 1.0
 */
@Documented
@Constraint(validatedBy = TradeExpiryValidator.class)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface TradeExpiryValid {

    /**
     * Default error message when validation fails.
     * 
     * @return the error message
     */
    String message() default "Trade has already expired based on maturity date";

    /**
     * Validation groups.
     * 
     * @return the validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Validation payload.
     * 
     * @return the validation payload
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Custom failure message for this validation.
     * 
     * @return the custom failure message
     */
    String failureMessage() default "Trade has already expired based on maturity date";
}
