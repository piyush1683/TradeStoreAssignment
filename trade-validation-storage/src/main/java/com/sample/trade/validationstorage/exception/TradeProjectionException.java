package com.sample.trade.validationstorage.exception;

/**
 * Exception thrown when there's an error in trade projection operations.
 * 
 * <p>
 * This exception is used to wrap and provide context for errors that occur
 * during trade projection store operations, validation processes, or other
 * trade projection related activities.
 * </p>
 * 
 * <p>
 * <strong>Common scenarios:</strong>
 * </p>
 * <ul>
 * <li>Database connection failures during trade projection updates</li>
 * <li>Validation errors during trade processing</li>
 * <li>Data integrity issues in trade projection store</li>
 * <li>Configuration or setup problems</li>
 * </ul>
 * 
 * @author Trade Store Team
 * @version 1.0
 * @since 1.0
 */
public class TradeProjectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new TradeProjectionException with the specified detail message.
     * 
     * @param message the detail message explaining the cause of the exception
     */
    public TradeProjectionException(String message) {
        super(message);
    }

    /**
     * Constructs a new TradeProjectionException with the specified detail message
     * and cause.
     * 
     * @param message the detail message explaining the cause of the exception
     * @param cause   the cause of this exception
     */
    public TradeProjectionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new TradeProjectionException with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public TradeProjectionException(Throwable cause) {
        super(cause);
    }
}
