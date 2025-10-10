package com.sample.trade.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for validating trade maturity dates.
 * 
 * <p>This annotation ensures that the trade maturity date is not in the past
 * and is a valid business date according to trade validation rules.</p>
 * 
 * @author piyush.agrawal
 * @version 1.0
 * @since 1.0
 */
@Documented
@Constraint(validatedBy = MaturityDateValidator.class)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaturityDateValid {
    
    /**
     * Default error message when validation fails.
     * 
     * @return the error message
     */
    String message() default "Trade maturity date is in the past and not acceptable";
    
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
    String failureMessage() default "Trade maturity date is in the past and not acceptable";
}
