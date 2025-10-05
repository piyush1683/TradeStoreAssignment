package com.sample.trade.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.time.LocalDate;
import com.sample.trade.common.model.TradeException;
import com.sample.trade.common.store.TradeStore;

@Component
public class TradeQueryService {

    @Autowired
    private TradeStore tradeStore;

    public List<TradeException> getTradeExceptionsByRequestId(String requestId) {
        return tradeStore.getTradeExceptionsByRequestId(requestId);
    }

    public List<TradeException> getTradeExceptionsByTimeRange(LocalDate startDate, LocalDate endDate) {
        return tradeStore.getTradeExceptionsByTimeRange(startDate, endDate);
    }

    public List<TradeException> getTradeExceptionsByTradeId(String tradeId) {
        return tradeStore.getTradeExceptionsByTradeId(tradeId);
    }

}
