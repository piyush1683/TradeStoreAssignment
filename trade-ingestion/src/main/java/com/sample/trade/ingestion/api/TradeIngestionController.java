package com.sample.trade.ingestion.api;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.service.TradeQueryService;
import com.sample.trade.common.store.TradeStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sample.trade.ingestion.service.KafkaTradeIngestionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TradeIngestionController implements TradeIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(TradeIngestionController.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String TRADE_ACCEPTED_MESSAGE = "Trade accepted with requestId: ";

    private final KafkaTradeIngestionService kafkaTradeIngestionService;

    private final TradeQueryService tradeQueryService;

    private final TradeStore tradeStore;

    public TradeIngestionController(KafkaTradeIngestionService kafkaTradeIngestionService,
            TradeQueryService tradeQueryService, TradeStore tradeStore) {
        this.kafkaTradeIngestionService = kafkaTradeIngestionService;
        this.tradeQueryService = tradeQueryService;
        this.tradeStore = tradeStore;
    }

    @Override
    public void acceptTrade(Trade trade, String requestId) {
        try {
            trade.setRequestId(requestId);
            kafkaTradeIngestionService.sendTradetoKafka(trade);
        } catch (Exception e) {
            throw new TradeIngestionException("Failed to publish trade to Kafka", e);
        }
    }

    @Override
    public void acceptTrades(List<Trade> trades, String requestId) {
        for (Trade trade : trades) {
            acceptTrade(trade, requestId);
        }
    }

    @PostMapping("/trades")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> createTrades(@RequestBody List<Trade> trades) {
        String requestId = UUID.randomUUID().toString();
        acceptTrades(trades, requestId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(TRADE_ACCEPTED_MESSAGE + requestId);
    }

    @PostMapping("/trade")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> createTrade(@RequestBody Trade trade) {
        String requestId = UUID.randomUUID().toString();
        acceptTrade(trade, requestId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(TRADE_ACCEPTED_MESSAGE + requestId);
    }

    @PostMapping(value = "/trades-file/upload")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> uploadTradesCsv(MultipartFile file) throws IOException {
        List<Trade> trades = parseCsv(file);
        String requestId = UUID.randomUUID().toString();
        acceptTrades(trades, requestId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(TRADE_ACCEPTED_MESSAGE + requestId);
    }

    private static List<Trade> parseCsv(MultipartFile file) throws IOException {
        List<Trade> trades = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!StringUtils.hasText(line)) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 7) {
                    continue;
                }
                Trade t = new Trade();
                t.setTradeId(parts[0].trim());
                t.setVersion(Integer.parseInt(parts[1].trim()));
                t.setCounterPartyId(parts[2].trim());
                t.setBookId(parts[3].trim());
                t.setMaturityDate(LocalDate.parse(parts[4].trim(), DATE_FORMAT));
                t.setCreatedDate("<today date>".equalsIgnoreCase(parts[5].trim()) ? LocalDate.now()
                        : LocalDate.parse(parts[5].trim(), DATE_FORMAT));
                t.setExpired(parts[6].trim());
                trades.add(t);
            }
        }
        return trades;
    }

    @GetMapping(value = "/notifications")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Object> getTradeNotifications(
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String tradeId) {

        if (requestId != null && !"".equals(requestId)) {
            logger.info("Getting trade exceptions for requestId: {}", requestId);
            return ResponseEntity.ok()
                    .body(tradeQueryService.getTradeExceptionsByRequestId(requestId));
        } else if (tradeId != null && !"".equals(tradeId)) {
            return ResponseEntity.ok()
                    .body(tradeQueryService.getTradeExceptionsByTradeId(tradeId));
        } else {
            return ResponseEntity.badRequest()
                    .body("Please provide either 'requestId' or 'tradeId' parameter. " +
                            "Example: /notifications?requestId=req-123 or /notifications?tradeId=T1");
        }
    }

    // Integration Test Support Endpoints

    /**
     * Get trade by ID - for integration tests
     */
    @GetMapping(value = "/test/trade/{tradeId}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Trade> getTradeById(@PathVariable String tradeId) {
        try {
            logger.info("Getting trade by ID: {}", tradeId);
            Trade trade = tradeStore.getTradeById(tradeId);
            if (trade != null) {
                return ResponseEntity.ok(trade);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting trade by ID {}: {}", tradeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Clean up test data for trade IDs starting with 3XXX - for integration tests
     */
    @PostMapping(value = "/test/cleanup")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> cleanupTestData() {
        try {
            logger.info("Cleaning up test data for integration tests");
            tradeStore.cleanupTestData();
            return ResponseEntity.ok("Test data cleanup completed successfully");
        } catch (Exception e) {
            logger.error("Error during test data cleanup: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to cleanup test data: " + e.getMessage());
        }
    }

    /**
     * Clean up test data by pattern - for integration tests
     */
    @PostMapping(value = "/test/cleanup/{pattern}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> cleanupTestDataByPattern(@PathVariable String pattern) {
        try {
            logger.info("Cleaning up test data with pattern: {}", pattern);
            tradeStore.cleanupTestDataByPattern(pattern);
            return ResponseEntity.ok("Test data cleanup completed successfully for pattern: " + pattern);
        } catch (Exception e) {
            logger.error("Error during test data cleanup for pattern {}: {}", pattern, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to cleanup test data for pattern " + pattern + ": " + e.getMessage());
        }
    }
}
