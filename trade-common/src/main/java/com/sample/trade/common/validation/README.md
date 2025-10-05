# Trade Expiry Validation

This package contains validation utilities and annotations for trade expiry management in the Trade Store system.

## Overview

The trade expiry validation system provides a comprehensive solution for managing trade lifecycle, including validation, expiry checking, and automated status updates.

## Components

### 1. @ValidateTradeExpiry Annotation

A custom annotation that marks methods performing trade expiry validation.

```java
@ValidateTradeExpiry(
    description = "Validates if trade has exceeded its maturity date",
    priority = 1,
    critical = true
)
public boolean isTradeExpired(Trade trade) {
    // Validation logic
}
```

#### Annotation Parameters

- **description**: Human-readable description of the validation logic
- **priority**: Priority level (higher number = higher priority)
- **critical**: Whether validation failure should stop processing

### 2. TradeExpiryValidationAspect

An AspectJ aspect that provides cross-cutting concerns for annotated methods:

- Performance monitoring
- Result logging and auditing
- Error handling and exception management
- Validation metrics collection

## Usage Examples

### Basic Expiry Validation

```java
@Service
public class TradeService {
    
    @ValidateTradeExpiry(description = "Check if trade has expired")
    public boolean isTradeExpired(Trade trade) {
        return trade.getMaturityDate().isBefore(LocalDate.now());
    }
}
```

### Advanced Expiry Management

```java
@Service
public class TradeProjectionServiceImpl {
    
    @ValidateTradeExpiry(
        description = "Comprehensive trade expiry check and status update",
        priority = 1,
        critical = true
    )
    public boolean checkAndMarkTradeAsExpired(Trade trade) {
        if (isTradeExpired(trade)) {
            tradeStore.markTradeAsExpired(trade.getTradeId());
            return true;
        }
        return false;
    }
}
```

## Integration with Spring AOP

To enable the aspect functionality, add the following to your Spring configuration:

```java
@Configuration
@EnableAspectJAutoProxy
public class ValidationConfig {
    // Configuration
}
```

## Validation Rules

### Expiry Validation Logic

1. **Null Check**: Trade and maturity date must not be null
2. **Date Comparison**: Maturity date is compared with current date
3. **Expiry Determination**: Trade is expired if maturity date < today

### Business Rules

- **Active Trade**: Maturity date >= today
- **Expired Trade**: Maturity date < today
- **Invalid Trade**: Null trade or null maturity date

## Error Handling

The validation system provides comprehensive error handling:

- **Null Safety**: Graceful handling of null inputs
- **Exception Management**: Proper exception logging and propagation
- **Critical Validation**: Configurable error handling based on criticality

## Logging

The system provides detailed logging at different levels:

- **DEBUG**: Detailed validation process information
- **INFO**: Successful validation results
- **WARN**: Validation failures and non-critical issues
- **ERROR**: Critical validation errors and exceptions

## Performance Considerations

- **Execution Time Monitoring**: Built-in performance measurement
- **Efficient Date Operations**: Optimized date comparison logic
- **Minimal Overhead**: Lightweight annotation processing

## Testing

The validation methods can be easily unit tested:

```java
@Test
public void testTradeExpiryValidation() {
    Trade expiredTrade = new Trade("T1", 1, "CP1", "B1", 
                                 LocalDate.now().minusDays(1), 
                                 LocalDate.now(), "N", "req1");
    
    boolean isExpired = tradeService.isTradeExpired(expiredTrade);
    assertTrue(isExpired);
}
```

## Best Practices

1. **Always check for null inputs** before validation
2. **Use descriptive annotation descriptions** for better documentation
3. **Set appropriate priority levels** for validation methods
4. **Mark critical validations** appropriately
5. **Include comprehensive logging** for debugging and monitoring
6. **Write unit tests** for all validation methods

## Dependencies

- Spring Framework (for AOP support)
- AspectJ (for aspect weaving)
- SLF4J (for logging)
- Java 8+ (for LocalDate support)

## Future Enhancements

- **Validation Metrics**: Collection and reporting of validation statistics
- **Custom Validation Rules**: Pluggable validation rule system
- **Batch Validation**: Support for validating multiple trades at once
- **Validation Caching**: Caching of validation results for performance
