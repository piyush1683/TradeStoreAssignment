package com.sample.trade.common.validation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Unified aspect for handling trade validation methods annotated
 * with @ValidateTradeVersion and @ValidateTradeExpiry.
 * 
 * <p>
 * This aspect provides cross-cutting concerns for all trade validation methods
 * including:
 * </p>
 * <ul>
 * <li>Method execution timing and performance monitoring</li>
 * <li>Validation result logging and auditing</li>
 * <li>Error handling and exception management</li>
 * <li>Validation metrics collection</li>
 * </ul>
 * 
 * <p>
 * <strong>Usage:</strong>
 * </p>
 * <p>
 * This aspect automatically intercepts methods annotated
 * with @ValidateTradeVersion or @ValidateTradeExpiry
 * and provides additional functionality without modifying the original method
 * code.
 * </p>
 * 
 * <p>
 * <strong>Note:</strong>
 * </p>
 * <p>
 * To enable this aspect, ensure that AspectJ is properly configured in your
 * Spring context
 * and that the @EnableAspectJAutoProxy annotation is present on your
 * configuration class.
 * </p>
 * 
 * @author Trade Store Team
 * @version 1.0
 * @since 1.0
 */
@Aspect
@Component
public class TradeValidationAspect {

    private static final Logger logger = LoggerFactory.getLogger(TradeValidationAspect.class);

    /**
     * Around advice for methods annotated with @ValidateTradeVersion.
     * 
     * <p>
     * This method provides cross-cutting functionality for trade version validation
     * methods,
     * including performance monitoring, result logging, and error handling.
     * </p>
     * 
     * @param joinPoint            the join point representing the method execution
     * @param validateTradeVersion the annotation instance
     * @return the result of the method execution
     * @throws Throwable if the method execution throws an exception
     */
    @Around("@annotation(validateTradeVersion)")
    public Object aroundTradeVersionValidation(ProceedingJoinPoint joinPoint,
            ValidateTradeVersion validateTradeVersion) throws Throwable {

        String methodName = joinPoint.getSignature().getName();
        String description = validateTradeVersion.description();
        int priority = validateTradeVersion.priority();
        boolean critical = validateTradeVersion.critical();
        ValidateTradeVersion.VersionValidationAction onFailure = validateTradeVersion.onFailure();

        logger.debug("Starting trade version validation: {} (Priority: {}, Critical: {}, OnFailure: {})",
                methodName, priority, critical, onFailure);

        long startTime = System.currentTimeMillis();

        try {
            // Execute the validation method
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            // Log validation result
            if (result instanceof Boolean booleanResult) {
                if (booleanResult.booleanValue()) {
                    logger.info("Trade version validation PASSED: {} - {} (Execution time: {}ms)",
                            methodName, description, executionTime);
                } else {
                    logger.warn("Trade version validation FAILED: {} - {} (Execution time: {}ms, Action: {})",
                            methodName, description, executionTime, onFailure);
                }
            } else {
                logger.info("Trade version validation completed: {} - {} (Execution time: {}ms)",
                        methodName, description, executionTime);
            }

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            logger.error("Trade version validation ERROR: {} - {} (Execution time: {}ms, Error: {}, Action: {})",
                    methodName, description, executionTime, e.getMessage(), onFailure);

            // If the validation is critical, re-throw the exception
            if (critical) {
                logger.error("Critical version validation failed, propagating exception", e);
                throw e;
            } else {
                logger.warn("Non-critical version validation failed, returning false", e);
                return false;
            }
        }
    }

    /**
     * Around advice for methods annotated with @ValidateTradeExpiry.
     * 
     * <p>
     * This method provides cross-cutting functionality for trade expiry validation
     * methods,
     * including performance monitoring, result logging, and error handling.
     * </p>
     * 
     * @param joinPoint           the join point representing the method execution
     * @param validateTradeExpiry the annotation instance
     * @return the result of the method execution
     * @throws Throwable if the method execution throws an exception
     */
    @Around("@annotation(validateTradeExpiry)")
    public Object aroundTradeExpiryValidation(ProceedingJoinPoint joinPoint,
            ValidateTrade validateTradeExpiry) throws Throwable {

        String methodName = joinPoint.getSignature().getName();
        String description = validateTradeExpiry.description();
        int priority = validateTradeExpiry.priority();
        boolean critical = validateTradeExpiry.critical();

        logger.debug("Starting trade expiry validation: {} (Priority: {}, Critical: {})",
                methodName, priority, critical);

        long startTime = System.currentTimeMillis();

        try {
            // Execute the validation method
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            // Log validation result
            if (result instanceof Boolean booleanResult) {
                if (booleanResult.booleanValue()) {
                    logger.info("Trade expiry validation PASSED: {} - {} (Execution time: {}ms)",
                            methodName, description, executionTime);
                } else {
                    logger.warn("Trade expiry validation FAILED: {} - {} (Execution time: {}ms)",
                            methodName, description, executionTime);
                }
            } else {
                logger.info("Trade expiry validation completed: {} - {} (Execution time: {}ms)",
                        methodName, description, executionTime);
            }

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            logger.error("Trade expiry validation ERROR: {} - {} (Execution time: {}ms, Error: {})",
                    methodName, description, executionTime, e.getMessage());

            // If the validation is critical, re-throw the exception
            if (critical) {
                logger.error("Critical expiry validation failed, propagating exception", e);
                throw e;
            } else {
                logger.warn("Non-critical expiry validation failed, returning false", e);
                return false;
            }
        }
    }
}
