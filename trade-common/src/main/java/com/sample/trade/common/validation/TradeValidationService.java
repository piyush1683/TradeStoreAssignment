package com.sample.trade.common.validation;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.store.TradeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Unified service for comprehensive trade validation including version and
 * expiry validation.
 * 
 * <p>
 * This service provides a centralized validation system that handles all trade
 * validation
 * rules including version control, maturity date validation, and expiry
 * checking.
 * It combines the functionality of both version and expiry validation services
 * into a single, cohesive validation framework.
 * </p>
 * 
 * <p>
 * <strong>Key Features:</strong>
 * </p>
 * <ul>
 * <li>Version validation with configurable actions</li>
 * <li>Expiry validation based on maturity dates</li>
 * <li>Comprehensive validation with detailed error reporting</li>
 * <li>Configurable validation rules and actions</li>
 * <li>Extensive logging and performance monitoring</li>
 * </ul>
 * 
 * @author Trade Store Team
 * @version 1.0
 * @since 1.0
 */
@Service
public class TradeValidationService {

    private static final Logger logger = LoggerFactory.getLogger(TradeValidationService.class);

    private final TradeStore tradeStore;

    public TradeValidationService(TradeStore tradeStore) {
        this.tradeStore = tradeStore;
    }

    /**
     * Validates if a trade version is acceptable according to business rules.
     * 
     * <p>
     * This method implements the core version validation logic:
     * </p>
     * <ul>
     * <li>Lower versions are rejected</li>
     * <li>Same versions are allowed to replace existing trades</li>
     * <li>Higher versions are accepted</li>
     * </ul>
     * 
     * @param trade the trade to validate
     * @return true if the version is acceptable, false otherwise
     */
    @ValidateTradeVersion(description = "Validates if trade version is acceptable according to business rules", priority = 1, critical = true, onFailure = ValidateTradeVersion.VersionValidationAction.REJECT, allowSameVersionReplacement = true)
    public boolean isVersionValid(Trade trade) {
        if (trade == null) {
            logger.warn("Cannot validate version for null trade");
            return false;
        }

        if (trade.getTradeId() == null) {
            logger.warn("Cannot validate version for trade with null trade ID");
            return false;
        }

        try {
            Integer latestVersion = tradeStore.getLatestVersion(trade.getTradeId());
            int currentVersion = trade.getVersion();

            // If no previous version exists, any version is valid
            if (latestVersion == null) {
                logger.debug("No previous version found for trade {}, accepting version {}",
                        trade.getTradeId(), currentVersion);
                return true;
            }

            // Check version rules
            if (currentVersion < latestVersion) {
                logger.warn("Rejecting trade: Lower version received. Trade: {}, Current: {}, Latest: {}",
                        trade.getTradeId(), currentVersion, latestVersion);
                return false;
            }

            if (currentVersion == latestVersion) {
                logger.info("Accepting trade: Same version received. Trade: {}, Version: {} (will replace existing)",
                        trade.getTradeId(), currentVersion);
                return true;
            }

            // Higher version
            logger.info("Accepting trade: Higher version received. Trade: {}, Current: {}, Previous: {}",
                    trade.getTradeId(), currentVersion, latestVersion);
            return true;

        } catch (Exception e) {
            logger.error("Error validating trade version for trade {}: {}",
                    trade.getTradeId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validates if a trade has expired based on its maturity date.
     * 
     * <p>
     * This method checks if the trade's maturity date has passed the current date.
     * A trade is considered expired if its maturity date is before today's date.
     * </p>
     * 
     * <p>
     * <strong>Business Logic:</strong>
     * </p>
     * <ul>
     * <li>If maturity date is before today: Trade is expired</li>
     * <li>If maturity date is today or in the future: Trade is active</li>
     * </ul>
     * 
     * @param trade the trade to check for expiry
     * @return true if the trade has expired, false if it's still active
     */
    @ValidateTrade(description = "Validates if trade has exceeded its maturity date and should be marked as expired", priority = 2, critical = true)
    public boolean isTradeExpired(Trade trade) {
        if (trade == null) {
            logger.warn("Cannot check expiry for null trade");
            return false;
        }

        if (trade.getMaturityDate() == null) {
            logger.warn("Cannot check expiry for trade with null maturity date: {}", trade.getTradeId());
            return false;
        }

        LocalDate today = LocalDate.now();
        boolean isExpired = trade.getMaturityDate().isBefore(today);

        if (isExpired) {
            logger.info("Trade {} has expired. Maturity date: {}, Today: {}",
                    trade.getTradeId(), trade.getMaturityDate(), today);
        } else {
            logger.debug("Trade {} is still active. Maturity date: {}, Today: {}",
                    trade.getTradeId(), trade.getMaturityDate(), today);
        }

        return isExpired;
    }

    /**
     * Validates if a trade's maturity date is in the past.
     * 
     * <p>
     * This method checks if the trade's maturity date is before today's date.
     * Trades with past maturity dates are considered invalid for processing.
     * </p>
     * 
     * @param trade the trade to validate
     * @return true if the maturity date is valid (not in the past), false otherwise
     */
    @ValidateTrade(description = "Validates if trade maturity date is not in the past", priority = 3, critical = true)
    public boolean isMaturityDateValid(Trade trade) {
        if (trade == null) {
            logger.warn("Cannot validate maturity date for null trade");
            return false;
        }

        if (trade.getMaturityDate() == null) {
            logger.warn("Cannot validate maturity date for trade with null maturity date: {}", trade.getTradeId());
            return false;
        }

        LocalDate today = LocalDate.now();
        boolean isValid = !trade.getMaturityDate().isBefore(today);

        if (!isValid) {
            logger.warn("Trade {} has invalid maturity date. Maturity: {}, Today: {}",
                    trade.getTradeId(), trade.getMaturityDate(), today);
        } else {
            logger.debug("Trade {} has valid maturity date. Maturity: {}, Today: {}",
                    trade.getTradeId(), trade.getMaturityDate(), today);
        }

        return isValid;
    }

    /**
     * Performs comprehensive trade validation including all business rules.
     * 
     * <p>
     * This method validates a trade against all business rules:
     * </p>
     * <ul>
     * <li>Version validation</li>
     * <li>Maturity date validation</li>
     * <li>Expiry validation</li>
     * </ul>
     * 
     * @param trade the trade to validate
     * @return true if the trade passes all validation rules, false otherwise
     */
    @ValidateTrade(description = "Comprehensive trade validation including version, maturity, and expiry checks", priority = 1, critical = true)
    public boolean validateTrade(Trade trade) {
        if (trade == null) {
            logger.warn("Cannot validate null trade");
            return false;
        }

        try {
            // Rule 1: Check version validation
            if (!isVersionValid(trade)) {
                logger.warn("Trade validation failed: Version validation failed for trade {}", trade.getTradeId());
                return false;
            }

            // Rule 2: Check maturity date validation
            if (!isMaturityDateValid(trade)) {
                logger.warn("Trade validation failed: Maturity date validation failed for trade {}",
                        trade.getTradeId());
                return false;
            }

            // Rule 3: Check expiry validation
            if (isTradeExpired(trade)) {
                logger.warn("Trade validation failed: Trade has already expired for trade {}", trade.getTradeId());
                return false;
            }

            logger.info("Trade validation passed for trade {}", trade.getTradeId());
            return true;

        } catch (Exception e) {
            logger.error("Error during comprehensive trade validation for trade {}: {}",
                    trade.getTradeId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if a trade should be marked as expired and updates its status.
     * 
     * <p>
     * This method performs the complete expiry check and update process:
     * </p>
     * <ol>
     * <li>Validates if the trade has expired using
     * {@link #isTradeExpired(Trade)}</li>
     * <li>If expired, updates the trade's expired status in the database</li>
     * <li>Logs the expiry action for audit purposes</li>
     * </ol>
     * 
     * @param trade the trade to check and potentially mark as expired
     * @return true if the trade was marked as expired, false if it's still active
     */
    @ValidateTrade(description = "Checks trade expiry and updates expired status in the database", priority = 1, critical = true)
    public boolean checkAndMarkTradeAsExpired(Trade trade) {
        if (trade == null) {
            logger.warn("Cannot check expiry for null trade");
            return false;
        }

        try {
            if (isTradeExpired(trade)) {
                // Mark trade as expired in the database
                tradeStore.markTradeAsExpired(trade.getTradeId());
                logger.info("Successfully marked trade {} as expired", trade.getTradeId());
                return true;
            } else {
                logger.debug("Trade {} is still active, no expiry action needed", trade.getTradeId());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error checking and marking trade as expired: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validates trade version with custom failure action.
     * 
     * <p>
     * This method allows for configurable behavior when version validation fails,
     * supporting different business requirements.
     * </p>
     * 
     * @param trade                       the trade to validate
     * @param onFailure                   the action to take if validation fails
     * @param allowSameVersionReplacement whether to allow same version replacement
     * @return true if the version is acceptable, false otherwise
     */
    @ValidateTradeVersion(description = "Validates trade version with configurable failure action", priority = 2, critical = false)
    public boolean isVersionValidWithAction(Trade trade,
            ValidateTradeVersion.VersionValidationAction onFailure,
            boolean allowSameVersionReplacement) {
        if (trade == null) {
            logger.warn("Cannot validate version for null trade");
            return false;
        }

        try {
            Integer latestVersion = tradeStore.getLatestVersion(trade.getTradeId());
            int currentVersion = trade.getVersion();

            // If no previous version exists, any version is valid
            if (latestVersion == null) {
                logger.debug("No previous version found for trade {}, accepting version {}",
                        trade.getTradeId(), currentVersion);
                return true;
            }

            // Check version rules
            if (currentVersion < latestVersion) {
                logger.warn("Version validation failed: Lower version received. Trade: {}, Current: {}, Latest: {}",
                        trade.getTradeId(), currentVersion, latestVersion);

                switch (onFailure) {
                    case REJECT:
                        return false;
                    case ACCEPT_WITH_WARNING:
                        logger.warn("Accepting trade with lower version due to configuration: {}", trade.getTradeId());
                        return true;
                    case ACCEPT:
                        logger.info("Accepting trade with lower version due to configuration: {}", trade.getTradeId());
                        return true;
                    default:
                        return false;
                }
            }

            if (currentVersion == latestVersion) {
                if (allowSameVersionReplacement) {
                    logger.info(
                            "Accepting trade: Same version received. Trade: {}, Version: {} (will replace existing)",
                            trade.getTradeId(), currentVersion);
                    return true;
                } else {
                    logger.warn("Rejecting trade: Same version not allowed. Trade: {}, Version: {}",
                            trade.getTradeId(), currentVersion);
                    return false;
                }
            }

            // Higher version
            logger.info("Accepting trade: Higher version received. Trade: {}, Current: {}, Previous: {}",
                    trade.getTradeId(), currentVersion, latestVersion);
            return true;

        } catch (Exception e) {
            logger.error("Error validating trade version for trade {}: {}",
                    trade.getTradeId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if a trade version conflict exists.
     * 
     * @param trade the trade to check
     * @return true if there's a version conflict, false otherwise
     */
    @ValidateTradeVersion(description = "Checks if trade version conflict exists", priority = 3, critical = false)
    public boolean hasVersionConflict(Trade trade) {
        if (trade == null) {
            return false;
        }

        try {
            Integer latestVersion = tradeStore.getLatestVersion(trade.getTradeId());
            return latestVersion != null && trade.getVersion() < latestVersion;
        } catch (Exception e) {
            logger.error("Error checking version conflict for trade {}: {}",
                    trade.getTradeId(), e.getMessage());
            return false;
        }
    }

    /**
     * Gets the validation failure reason for comprehensive trade validation.
     * 
     * @param trade the trade that failed validation
     * @return detailed reason for validation failure
     */
    public String getValidationFailureReason(Trade trade) {
        if (trade == null) {
            return "No trade provided for validation";
        }

        try {
            // Check version validation failure
            if (!isVersionValid(trade)) {
                return getVersionValidationFailureReason(trade);
            }

            // Check maturity date validation failure
            if (!isMaturityDateValid(trade)) {
                return String.format("Maturity date in past: %s (Today: %s)",
                        trade.getMaturityDate(), LocalDate.now());
            }

            // Check expiry validation failure
            if (isTradeExpired(trade)) {
                return String.format("Trade has already expired. Maturity date: %s (Today: %s)",
                        trade.getMaturityDate(), LocalDate.now());
            }

            return "Unknown validation failure - trade did not pass validation rules";
        } catch (Exception e) {
            logger.error("Error determining validation failure reason: {}", e.getMessage());
            return "Validation error: " + e.getMessage();
        }
    }

    /**
     * Gets the validation failure reason for version validation.
     * 
     * @param trade the trade that failed validation
     * @return detailed reason for version validation failure
     */
    public String getVersionValidationFailureReason(Trade trade) {
        if (trade == null) {
            return "No trade provided for version validation";
        }

        try {
            Integer latestVersion = tradeStore.getLatestVersion(trade.getTradeId());
            int currentVersion = trade.getVersion();

            if (latestVersion == null) {
                return "No previous version found for trade: " + trade.getTradeId();
            }

            if (currentVersion < latestVersion) {
                return String.format("Lower version received: %d < %d (Trade: %s)",
                        currentVersion, latestVersion, trade.getTradeId());
            }

            if (currentVersion == latestVersion) {
                return String.format("Same version received: %d (Trade: %s)",
                        currentVersion, trade.getTradeId());
            }

            return "Version validation passed: " + currentVersion + " > " + latestVersion;

        } catch (Exception e) {
            logger.error("Error determining version validation failure reason: {}", e.getMessage());
            return "Version validation error: " + e.getMessage();
        }
    }
}
