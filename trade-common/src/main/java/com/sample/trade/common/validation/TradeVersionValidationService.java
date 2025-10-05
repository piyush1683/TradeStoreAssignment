package com.sample.trade.common.validation;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.store.TradeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for validating trade version rules.
 * 
 * <p>
 * This service provides comprehensive version validation functionality for trades,
 * including validation against latest versions, conflict resolution, and
 * exception handling.
 * </p>
 * 
 * <p>
 * <strong>Key Features:</strong>
 * </p>
 * <ul>
 * <li>Version comparison against latest stored version</li>
 * <li>Configurable validation actions on failure</li>
 * <li>Support for same-version replacement</li>
 * <li>Comprehensive logging and error handling</li>
 * </ul>
 * 
 * @author Trade Store Team
 * @version 1.0
 * @since 1.0
 */
@Service
public class TradeVersionValidationService {

    private static final Logger logger = LoggerFactory.getLogger(TradeVersionValidationService.class);

    private final TradeStore tradeStore;

    public TradeVersionValidationService(TradeStore tradeStore) {
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
    @ValidateTradeVersion(
        description = "Validates if trade version is acceptable according to business rules",
        priority = 1,
        critical = true,
        onFailure = ValidateTradeVersion.VersionValidationAction.REJECT,
        allowSameVersionReplacement = true
    )
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
     * Validates trade version with custom failure action.
     * 
     * <p>
     * This method allows for configurable behavior when version validation fails,
     * supporting different business requirements.
     * </p>
     * 
     * @param trade the trade to validate
     * @param onFailure the action to take if validation fails
     * @param allowSameVersionReplacement whether to allow same version replacement
     * @return true if the version is acceptable, false otherwise
     */
    @ValidateTradeVersion(
        description = "Validates trade version with configurable failure action",
        priority = 2,
        critical = false
    )
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
                    logger.info("Accepting trade: Same version received. Trade: {}, Version: {} (will replace existing)", 
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
     * Gets the validation failure reason for version validation.
     * 
     * @param trade the trade that failed validation
     * @return detailed reason for validation failure
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

    /**
     * Checks if a trade version conflict exists.
     * 
     * @param trade the trade to check
     * @return true if there's a version conflict, false otherwise
     */
    @ValidateTradeVersion(
        description = "Checks if trade version conflict exists",
        priority = 3,
        critical = false
    )
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
}
