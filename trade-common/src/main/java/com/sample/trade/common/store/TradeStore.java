package com.sample.trade.common.store;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.model.TradeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TradeStore {
    private static final Logger logger = LoggerFactory.getLogger(TradeStore.class);

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TradeStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get the latest version of a trade by trade ID
     */
    public Integer getLatestVersion(String tradeId) {
        String sql = "SELECT version FROM trade_projection WHERE trade_id = ? ORDER BY version DESC LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, tradeId);
        } catch (Exception e) {
            return null; // No existing trade found
        }
    }

    /**
     * Insert or update a valid trade in the projection table
     */
    public void insertTrade(Trade trade) {
        String upsertSql = """
                INSERT INTO trade_projection (trade_id, version, counter_party_id, book_id, maturity_date, created_date, expired)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (trade_id, version)
                DO UPDATE SET
                    counter_party_id = EXCLUDED.counter_party_id,
                    book_id = EXCLUDED.book_id,
                    maturity_date = EXCLUDED.maturity_date,
                    created_date = EXCLUDED.created_date,
                    expired = EXCLUDED.expired
                """;

        jdbcTemplate.update(upsertSql,
                trade.getTradeId(),
                trade.getVersion(),
                trade.getCounterPartyId(),
                trade.getBookId(),
                trade.getMaturityDate(),
                trade.getCreatedDate(),
                trade.getExpired());

        logger.info("Successfully updated trade projection: {}", trade.getTradeId());
    }

    /**
     * Insert a rejected trade into the exception table
     */
    public void insertTradeException(Trade trade, String exceptionReason) {
        String insertExceptionSql = """
                INSERT INTO trade_exception (trade_id, request_id, version, counter_party_id, book_id, maturity_date, created_date, expired, exception_reason, created_at)
                VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(insertExceptionSql,
                trade.getTradeId(),
                trade.getRequestId(),
                trade.getVersion(),
                trade.getCounterPartyId(),
                trade.getBookId(),
                trade.getMaturityDate(),
                trade.getCreatedDate(),
                trade.getExpired(),
                exceptionReason,
                LocalDateTime.now());

        logger.info("Trade rejected and stored in exception table: {}", trade.getTradeId());
    }

    /**
     * Get all trades from the projection table
     */
    public List<Trade> getAllTrades() {
        String sql = "SELECT trade_id, version, counter_party_id, book_id, maturity_date, created_date, expired FROM trade_projection";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Trade trade = new Trade();
            trade.setTradeId(rs.getString("trade_id"));
            trade.setVersion(rs.getInt("version"));
            trade.setCounterPartyId(rs.getString("counter_party_id"));
            trade.setBookId(rs.getString("book_id"));
            trade.setMaturityDate(rs.getDate("maturity_date").toLocalDate());
            trade.setCreatedDate(rs.getDate("created_date").toLocalDate());
            trade.setExpired(rs.getString("expired"));
            return trade;
        });
    }

    /**
     * Update the expired status of a trade
     */
    public void updateTradeExpiry(String tradeId, int version, String expired) {
        String sql = "UPDATE trade_projection SET expired = ? WHERE trade_id = ? AND version = ?";
        jdbcTemplate.update(sql, expired, tradeId, version);
        logger.info("Updated trade expiry status: {} version {} to {}", tradeId, version, expired);
    }

    /**
     * Get trades that need to be expired (maturity date < today and not already
     * expired)
     */
    public List<Trade> getTradesToExpire() {
        String sql = """
                SELECT trade_id, version, counter_party_id, book_id, maturity_date, created_date, expired
                FROM trade_projection
                WHERE maturity_date < ? AND expired = 'N'
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Trade trade = new Trade();
            trade.setTradeId(rs.getString("trade_id"));
            trade.setVersion(rs.getInt("version"));
            trade.setCounterPartyId(rs.getString("counter_party_id"));
            trade.setBookId(rs.getString("book_id"));
            trade.setMaturityDate(rs.getDate("maturity_date").toLocalDate());
            trade.setCreatedDate(rs.getDate("created_date").toLocalDate());
            trade.setExpired(rs.getString("expired"));
            return trade;
        }, LocalDate.now());
    }

    public List<String> getActiveTradeIds() {
        String sql = "SELECT DISTINCT trade_id FROM trade_projection WHERE expired = 'N'";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public Trade getTradeById(String tradeId) {
        String sql = "SELECT * FROM trade_projection WHERE trade_id = ? ORDER BY version DESC LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Trade trade = new Trade();
                trade.setTradeId(rs.getString("trade_id"));
                trade.setVersion(rs.getInt("version"));
                trade.setCounterPartyId(rs.getString("counter_party_id"));
                trade.setBookId(rs.getString("book_id"));
                trade.setMaturityDate(rs.getDate("maturity_date").toLocalDate());
                trade.setCreatedDate(rs.getDate("created_date").toLocalDate());
                trade.setExpired(rs.getString("expired"));
                return trade;
            }, tradeId);
        } catch (Exception e) {
            return null;
        }
    }

    public void markTradeAsExpired(String tradeId) {
        String sql = "UPDATE trade_projection SET expired = 'Y' WHERE trade_id = ? AND expired = 'N'";
        jdbcTemplate.update(sql, tradeId);
    }

    /**
     * Get trade exception records by request ID
     * Searches in both trade_id and exception_reason fields for the request ID
     * pattern
     */
    public List<TradeException> getTradeExceptionsByRequestId(String requestId) {
        String sql = """
                SELECT id, trade_id, request_id, version, counter_party_id, book_id, maturity_date, created_date, expired, exception_reason, created_at
                FROM trade_exception
                WHERE trade_id LIKE ? OR exception_reason LIKE ?
                ORDER BY created_at DESC
                """;
        String requestIdPattern = "%" + requestId + "%";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new TradeException(
                    rs.getLong("id"),
                    rs.getString("trade_id"),
                    rs.getString("request_id"),
                    rs.getInt("version"),
                    rs.getString("counter_party_id"),
                    rs.getString("book_id"),
                    rs.getDate("maturity_date").toLocalDate(),
                    rs.getDate("created_date").toLocalDate(),
                    rs.getString("expired"),
                    rs.getString("exception_reason"),
                    rs.getTimestamp("created_at").toLocalDateTime());
        }, requestIdPattern, requestIdPattern);
    }

    /**
     * Get trade exception records within a time range
     */
    public List<TradeException> getTradeExceptionsByTimeRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT id, trade_id, request_id, version, counter_party_id, book_id, maturity_date, created_date, expired, exception_reason, created_at
                FROM trade_exception
                WHERE created_at >= ? AND created_at <= ?
                ORDER BY created_at DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new TradeException(rs.getLong("id"), rs.getString("trade_id"),
                    rs.getString("request_id"), rs.getInt("version"), rs.getString("counter_party_id"),
                    rs.getString("book_id"), rs.getDate("maturity_date").toLocalDate(),
                    rs.getDate("created_date").toLocalDate(), rs.getString("expired"), rs.getString("exception_reason"),
                    rs.getTimestamp("created_at").toLocalDateTime());
        }, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }

    /**
     * Get trade exception records by trade ID
     */
    public List<TradeException> getTradeExceptionsByTradeId(String tradeId) {
        String sql = """
                SELECT id, trade_id, request_id, version, counter_party_id, book_id, maturity_date, created_date, expired, exception_reason, created_at
                FROM trade_exception
                WHERE trade_id = ?
                ORDER BY created_at DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new TradeException(rs.getLong("id"), rs.getString("trade_id"), rs.getString("request_id"),
                    rs.getInt("version"), rs.getString("counter_party_id"), rs.getString("book_id"),
                    rs.getDate("maturity_date").toLocalDate(), rs.getDate("created_date").toLocalDate(),
                    rs.getString("expired"), rs.getString("exception_reason"),
                    rs.getTimestamp("created_at").toLocalDateTime());
        }, tradeId);
    }

    /**
     * Clean up test data for trade IDs starting with 3XXX
     * This method is specifically for integration test cleanup
     */
    public void cleanupTestData() {
        try {
            // Delete from trade_exception table first (due to potential foreign key
            // constraints)
            String deleteExceptionsSql = "DELETE FROM trade_exception WHERE trade_id LIKE '3%'";
            int exceptionCount = jdbcTemplate.update(deleteExceptionsSql);
            logger.info("Cleaned up {} exception records for test data", exceptionCount);

            // Delete from trade_projection table
            String deleteProjectionsSql = "DELETE FROM trade_projection WHERE trade_id LIKE '3%'";
            int projectionCount = jdbcTemplate.update(deleteProjectionsSql);
            logger.info("Cleaned up {} projection records for test data", projectionCount);

        } catch (Exception e) {
            logger.error("Error during test data cleanup: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to cleanup test data", e);
        }
    }

    /**
     * Clean up test data for specific trade ID pattern
     * 
     * @param tradeIdPattern SQL LIKE pattern for trade IDs to delete
     */
    public void cleanupTestDataByPattern(String tradeIdPattern) {
        try {
            // Delete from trade_exception table first
            String deleteExceptionsSql = "DELETE FROM trade_exception WHERE trade_id LIKE ?";
            int exceptionCount = jdbcTemplate.update(deleteExceptionsSql, tradeIdPattern);
            logger.info("Cleaned up {} exception records for pattern: {}", exceptionCount, tradeIdPattern);

            // Delete from trade_projection table
            String deleteProjectionsSql = "DELETE FROM trade_projection WHERE trade_id LIKE ?";
            int projectionCount = jdbcTemplate.update(deleteProjectionsSql, tradeIdPattern);
            logger.info("Cleaned up {} projection records for pattern: {}", projectionCount, tradeIdPattern);

            logger.info("Test data cleanup completed for pattern: {}", tradeIdPattern);
        } catch (Exception e) {
            logger.error("Error during test data cleanup for pattern {}: {}", tradeIdPattern, e.getMessage(), e);
            throw new RuntimeException("Failed to cleanup test data for pattern: " + tradeIdPattern, e);
        }
    }
}
