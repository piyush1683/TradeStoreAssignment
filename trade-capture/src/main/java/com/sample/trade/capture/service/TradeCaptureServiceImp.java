package com.sample.trade.capture.service;

import com.sample.trade.common.model.Trade;
import com.sample.trade.capture.model.TradeModel;
import com.sample.trade.validationstorage.service.TradeProjectionService;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TradeCaptureServiceImp implements TradeCaptureService {

    private static final Logger logger = LoggerFactory.getLogger(TradeCaptureServiceImp.class);

    private final DynamoDBMapper dynamoDBMapper;
    private final AmazonDynamoDB amazonDynamoDB;

    @Autowired
    private TradeProjectionService tradeProjectionService;

    @Value("${aws.dynamodb.table-name}")
    private String tableName;

    public TradeCaptureServiceImp(DynamoDBMapper dynamoDBMapper, AmazonDynamoDB amazonDynamoDB) {
        this.dynamoDBMapper = dynamoDBMapper;
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    public void persistTrade(Trade trade) {
        try {
            // Basic null check only
            if (trade == null) {
                logger.warn("Cannot persist null trade");
                return;
            }

            logger.info("Persisting trade to DynamoDB: {}", trade.getTradeId());

            // Convert Trade to TradeModel for DynamoDB persistence
            TradeModel tradeModel = new TradeModel();
            tradeModel.reqtradeid = trade.getRequestId() + "#" + trade.getTradeId() + "#" + trade.getVersion();
            tradeModel.tradeId = trade.getTradeId();
            tradeModel.version = trade.getVersion();
            tradeModel.counterPartyId = trade.getCounterPartyId();
            tradeModel.bookId = trade.getBookId();
            tradeModel.maturityDate = trade.getMaturityDate();
            tradeModel.createdDate = trade.getCreatedDate();
            tradeModel.expired = trade.getExpired();

            // Use low-level DynamoDB client for persistence
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("reqtradeid", new AttributeValue().withS(tradeModel.reqtradeid));
            item.put("tradeId", new AttributeValue().withS(tradeModel.tradeId));
            item.put("version", new AttributeValue().withN(String.valueOf(tradeModel.version)));
            item.put("counterPartyId", new AttributeValue().withS(tradeModel.counterPartyId));
            item.put("bookId", new AttributeValue().withS(tradeModel.bookId));
            item.put("maturityDate", new AttributeValue().withS(tradeModel.maturityDate.toString()));
            item.put("createdDate", new AttributeValue().withS(tradeModel.createdDate.toString()));
            item.put("expired", new AttributeValue().withS(tradeModel.expired));

            PutItemRequest putItemRequest = new PutItemRequest()
                    .withTableName(tableName)
                    .withItem(item);

            amazonDynamoDB.putItem(putItemRequest);
            logger.info("Successfully persisted trade: {} to DynamoDB", trade.getTradeId());

            // Update trade projection in validation-storage service
            if (tradeProjectionService != null) {
                try {
                    tradeProjectionService.updateTradeProjectStore(trade);
                    logger.info("Successfully updated trade projection for: {}", trade.getTradeId());
                } catch (Exception e) {
                    logger.error("Error updating trade projection: {}", e.getMessage());
                    // Don't fail the entire operation if projection update fails
                }
            } else {
                logger.warn("TradeProjectionService not available - skipping projection update");
            }

        } catch (Exception e) {
            logger.error("Error persisting trade to DynamoDB: {}", e.getMessage());
            throw new RuntimeException("Failed to persist trade to DynamoDB", e);
        }
    }

    @Override
    @KafkaListener(topics = "trade_ingestion")
    public Trade readTradeMsgs(Trade trade) {
        logger.info("Received trade from Kafka: {}", trade);
        try {
            persistTrade(trade);
            return trade;
        } catch (Exception e) {
            logger.error("Error processing trade message: {}", e.getMessage());
            throw new RuntimeException("Failed to process trade message", e);
        }
    }
}
