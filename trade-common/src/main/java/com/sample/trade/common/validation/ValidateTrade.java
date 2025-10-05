package com.sample.trade.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that validate trade expiry.
 * 
 * <p>
 * This annotation can be used to indicate that a method performs trade expiry
 * validation.
 * It provides metadata about the validation process and can be used for
 * documentation,
 * logging, or AOP (Aspect-Oriented Programming) purposes.
 * </p>
 * 
 * <p>
 * <strong>Usage:</strong>
 * </p>
 * 
 * <pre>{@code
 * @ValidateTradeExpiry(description = "Validates if trade has exceeded maturity date")
 * public boolean isTradeExpired(Trade trade) {
 *     return trade.getMaturityDate().isBefore(LocalDate.now());
 * }
 * }</pre>
 * 
 * @author Trade Store Team
 * @version 1.0
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateTrade {

    /**
     * Description of what the expiry validation method does.
     * 
     * @return description of the validation logic
     */
    String description() default "Validates trade expiry based on maturity date";

    /**
     * Priority level for the validation (higher number = higher priority).
     * 
     * @return priority level (default: 1)
     */
    int priority() default 1;

    /**
     * Whether this validation is critical and should stop processing if it fails.
     * 
     * @return true if critical, false otherwise (default: true)
     */
    boolean critical() default true;
}
