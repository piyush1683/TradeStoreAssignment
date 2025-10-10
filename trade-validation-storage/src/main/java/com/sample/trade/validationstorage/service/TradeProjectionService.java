package com.sample.trade.validationstorage.service;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.model.TradeRecord;

public interface TradeProjectionService {

    public Trade readTradeEventStore();

    public void updateTradeProjectStore(Trade tradeModel);

    public void validateTrade(Trade tradeRecord);
}
