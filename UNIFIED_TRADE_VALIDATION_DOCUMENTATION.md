# Unified Trade Validation System Documentation

## Overview

This document describes the unified trade validation system that combines both version and expiry validation into a single, comprehensive service. The system provides robust validation mechanisms, reusable annotations, and comprehensive error handling for all trade validation requirements.

## Architecture

### Components

1. **@ValidateTradeVersion Annotation** - Custom annotation for version validation methods
2. **@ValidateTradeExpiry Annotation** - Custom annotation for expiry validation methods
3. **TradeValidationService** - Unified service combining all validation functionality
4. **TradeValidationAspect** - Unified AspectJ aspect for cross-cutting concerns
5. **Comprehensive Test Suite** - Unit tests for all validation functionality

## Key Features

### 1. Unified Validation Service

The `TradeValidationService` combines all validation functionality into a single service:

#### Version Validation Methods

- **`isVersionValid(Trade trade)`** - Basic version validation with business rules
- **`isVersionValidWithAction(Trade trade, VersionValidationAction onFailure, boolean allowSameVersionReplacement)`** - Configurable version validation
- **`hasVersionConflict(Trade trade)`** - Version conflict detection

#### Expiry Validation Methods

- **`isTradeExpired(Trade trade)`** - Expiry validation based on maturity date
- **`isMaturityDateValid(Trade trade)`** - Maturity date validation
- **`checkAndMarkTradeAsExpired(Trade trade)`** - Expiry check and database update

#### Comprehensive Validation

- **`validateTrade(Trade trade)`** - Complete validation including all business rules
- **`getValidationFailureReason(Trade trade)`** - Detailed failure reason analysis

### 2. Annotation System

#### @ValidateTradeVersion Annotation

```java
@ValidateTradeVersion(
    description = "Description of validation logic",
    priority = 1,           // Higher number = higher priority
    critical = true,        // Whether failure should stop processing
    onFailure = REJECT,     // Action to take on failure
    allowSameVersionReplacement = true  // Whether to allow same version replacement
)
public boolean validationMethod(Trade trade) {
    // Validation logic
}
```

#### @ValidateTradeExpiry Annotation

```java
@ValidateTradeExpiry(
    description = "Description of validation logic",
    priority = 1,           // Higher number = higher priority
    critical = true         // Whether failure should stop processing
)
public boolean validationMethod(Trade trade) {
    // Validation logic
}
```

### 3. Unified Aspect-Oriented Programming (AOP)

The `TradeValidationAspect` provides cross-cutting concerns for both annotation types:

- **Performance Monitoring**: Execution time measurement for all validation methods
- **Result Logging**: Detailed logging of validation results
- **Error Handling**: Exception management and propagation
- **Audit Trail**: Comprehensive logging for compliance

## Usage Examples

### Basic Validation

```java
@Service
public class TradeService {
    
    @Autowired
    private TradeValidationService tradeValidationService;
    
    public boolean processTrade(Trade trade) {
        // Comprehensive validation
        if (tradeValidationService.validateTrade(trade)) {
            // Process valid trade
            return true;
        } else {
            // Handle invalid trade
            String reason = tradeValidationService.getValidationFailureReason(trade);
            logger.warn("Trade validation failed: {}", reason);
            return false;
        }
    }
}
```

### Individual Validation Methods

```java
@Service
public class TradeProcessingService {
    
    @Autowired
    private TradeValidationService tradeValidationService;
    
    public void processTradeVersion(Trade trade) {
        if (tradeValidationService.isVersionValid(trade)) {
            // Process version validation
        }
    }
    
    public void processTradeExpiry(Trade trade) {
        if (tradeValidationService.isTradeExpired(trade)) {
            // Handle expired trade
        }
    }
}
```

### Configurable Version Validation

```java
@Service
public class TradeVersionService {
    
    @Autowired
    private TradeValidationService tradeValidationService;
    
    public boolean validateWithCustomAction(Trade trade) {
        return tradeValidationService.isVersionValidWithAction(
            trade, 
            VersionValidationAction.ACCEPT_WITH_WARNING, 
            true
        );
    }
}
```

## Validation Rules

### Version Validation Logic

1. **New Trade**: No previous version → Accept any version
2. **Lower Version**: Current < Latest → Reject (configurable action)
3. **Same Version**: Current = Latest → Accept (if replacement allowed)
4. **Higher Version**: Current > Latest → Accept

### Expiry Validation Logic

1. **Active Trade**: Maturity date >= today
2. **Expired Trade**: Maturity date < today
3. **Invalid Trade**: Null trade or null maturity date

### Comprehensive Validation Rules

1. **Version Control**: Reject trades with lower version numbers
2. **Maturity Date**: Reject trades with past maturity dates
3. **Expiry Check**: Reject trades that have already expired

## Integration with Spring AOP

To enable the aspect functionality, add the following to your Spring configuration:

```java
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.sample.trade")
public class TradeValidationConfig {
    
    @Bean
    public TradeValidationService tradeValidationService(TradeStore tradeStore) {
        return new TradeValidationService(tradeStore);
    }
}
```

## Error Handling

### Exception Hierarchy

- **TradeProjectionException**: Custom exception for trade projection errors
- **RuntimeException**: Wrapped exceptions with context
- **Validation Errors**: Graceful handling of validation failures

### Error Scenarios

1. **Null Input Handling**: Graceful handling of null trades and dates
2. **Database Errors**: Proper exception wrapping and logging
3. **Validation Failures**: Detailed error messages and logging
4. **Critical vs Non-Critical**: Different handling based on criticality

## Logging and Monitoring

### Log Levels

- **DEBUG**: Detailed validation process information
- **INFO**: Successful validation results and actions
- **WARN**: Validation failures and non-critical issues
- **ERROR**: Critical validation errors and exceptions

### Log Messages

```
INFO  - Trade version validation PASSED: isVersionValid - Validates if trade version is acceptable (Execution time: 2ms)
WARN  - Trade expiry validation FAILED: isTradeExpired - Validates if trade has exceeded its maturity date (Execution time: 1ms)
ERROR - Trade validation ERROR: validateTrade - Comprehensive trade validation (Execution time: 3ms, Error: NullPointerException)
```

## Testing

### Test Coverage

The system includes comprehensive unit tests covering:

- **Version Validation**: New, higher, same, and lower version scenarios
- **Expiry Validation**: Expired and active trade scenarios
- **Maturity Date Validation**: Valid and invalid date scenarios
- **Comprehensive Validation**: All business rules combined
- **Null Input Handling**: Graceful handling of null inputs
- **Configurable Actions**: Different failure action scenarios
- **Error Scenarios**: Exception handling and recovery

### Test Examples

```java
@Test
void testValidateTrade_WithValidTrade_ReturnsTrue() {
    Trade validTrade = new Trade("T1", 1, "CP1", "B1",
            LocalDate.now().plusDays(30),
            LocalDate.now(), "N", "req1");

    when(tradeStore.getLatestVersion("T1")).thenReturn(null);

    boolean isValid = tradeValidationService.validateTrade(validTrade);
    assertTrue(isValid);
}
```

## Configuration

### Dependencies

```gradle
dependencies {
    implementation 'org.springframework:spring-aop:6.1.0'
    implementation 'org.aspectj:aspectjweaver:1.9.20'
    // ... other dependencies
}
```

### Spring Configuration

```java
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.sample.trade")
public class TradeValidationConfig {
    
    @Bean
    public TradeValidationService tradeValidationService(TradeStore tradeStore) {
        return new TradeValidationService(tradeStore);
    }
}
```

## Performance Considerations

### Optimization Features

- **Unified Service**: Single service reduces object creation overhead
- **Efficient Validation**: Optimized validation logic and database queries
- **Minimal Overhead**: Lightweight annotation processing
- **Execution Time Monitoring**: Built-in performance measurement
- **Caching Support**: Ready for future caching implementation

### Performance Metrics

- **Method Execution Time**: Measured and logged for each validation
- **Success/Failure Rates**: Tracked through logging
- **Error Frequency**: Monitored for system health

## Best Practices

### Code Quality

1. **Use the unified service** for all validation needs
2. **Always check for null inputs** before validation
3. **Use descriptive annotation descriptions** for better documentation
4. **Set appropriate priority levels** for validation methods
5. **Mark critical validations** appropriately
6. **Include comprehensive logging** for debugging and monitoring

### Testing Guidelines

1. **Write unit tests** for all validation methods
2. **Test edge cases** including null inputs and boundary conditions
3. **Mock dependencies** for isolated testing
4. **Verify logging output** in tests
5. **Test exception scenarios** thoroughly

### Documentation Standards

1. **JavaDoc comments** for all public methods
2. **Inline comments** for complex business logic
3. **README files** for package documentation
4. **Usage examples** in documentation
5. **API documentation** for external consumers

## Migration from Separate Services

### Backward Compatibility

The unified service maintains backward compatibility:

- **Existing Code**: No changes required to existing validation logic
- **Gradual Migration**: Can be adopted incrementally
- **Configuration**: Flexible configuration options for different use cases

### Migration Steps

1. **Update Dependencies**: Replace individual validation services with unified service
2. **Update Bean Configuration**: Use `TradeValidationService` instead of separate services
3. **Update Constructor Calls**: Pass unified service to dependent classes
4. **Test Thoroughly**: Verify all validation functionality works as expected

## Future Enhancements

### Planned Features

- **Validation Metrics**: Collection and reporting of validation statistics
- **Custom Validation Rules**: Pluggable validation rule system
- **Batch Validation**: Support for validating multiple trades at once
- **Validation Caching**: Caching of validation results for performance
- **Real-time Monitoring**: Dashboard for validation metrics

### Integration Opportunities

- **Monitoring Systems**: Integration with APM tools
- **Alerting**: Real-time alerts for validation failures
- **Analytics**: Data analysis of validation patterns
- **Reporting**: Automated validation reports

## Troubleshooting

### Common Issues

1. **AspectJ Not Working**: Ensure @EnableAspectJAutoProxy is configured
2. **Dependencies Missing**: Check AspectJ and Spring AOP dependencies
3. **Annotation Not Recognized**: Verify package imports and classpath
4. **Performance Issues**: Monitor execution times and optimize if needed

### Debug Steps

1. **Check Logs**: Review validation logs for errors
2. **Verify Configuration**: Ensure Spring AOP is properly configured
3. **Test Dependencies**: Verify all required dependencies are present
4. **Monitor Performance**: Check execution times and resource usage

## Conclusion

The Unified Trade Validation System provides a robust, maintainable, and extensible solution for all trade validation requirements. By combining version and expiry validation into a single service with comprehensive annotations and AOP support, it ensures consistency, reusability, and maintainability across the entire application.

The system is designed to be easily maintainable, well-documented, and thoroughly tested, making it suitable for production use in financial trading systems. The unified approach reduces complexity while providing all the functionality needed for comprehensive trade validation.
