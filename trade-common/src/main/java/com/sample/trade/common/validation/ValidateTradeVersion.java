package com.sample.trade.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that validate trade version rules.
 * 
 * <p>
 * This annotation can be used to indicate that a method performs trade version
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
 * @ValidateTradeVersion(description = "Validates if trade version is acceptable")
 * public boolean isVersionValid(Trade trade) {
 *     return trade.getVersion() >= getLatestVersion(trade.getTradeId());
 * }
 * }</pre>
 * 
 * @author Trade Store Team
 * @version 1.0
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateTradeVersion {

    /**
     * Description of what the version validation method does.
     * 
     * @return description of the validation logic
     */
    String description() default "Validates trade version against latest version";

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

    /**
     * The action to take when version validation fails.
     * 
     * @return action to take on failure (default: REJECT)
     */
    VersionValidationAction onFailure() default VersionValidationAction.REJECT;

    /**
     * Whether to allow same version trades to replace existing ones.
     * 
     * @return true if same version should replace, false otherwise (default: true)
     */
    boolean allowSameVersionReplacement() default true;

    /**
     * Enumeration of possible actions when version validation fails.
     */
    enum VersionValidationAction {
        /**
         * Reject the trade and store in exception table.
         */
        REJECT,
        
        /**
         * Accept the trade but log a warning.
         */
        ACCEPT_WITH_WARNING,
        
        /**
         * Accept the trade without any special handling.
         */
        ACCEPT
    }
}
