package com.sample.trade.validationstorage.service;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.store.TradeStore;
import com.sample.trade.common.validation.TradeValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeProjectionServiceTest {

    @Mock
    private TradeStore tradeStore;

    @Mock
    private TradeValidationService tradeValidationService;

    private TradeProjectionServiceImpl tradeProjectionService;

    @BeforeEach
    void setUp() {
        tradeProjectionService = new TradeProjectionServiceImpl(tradeStore, tradeValidationService);
    }

    @Test
    void testUpdateTradeProjectStore_ValidTrade() {
        // Given
        Trade trade = createSampleTrade("T1", 1, "CP-1", "B1",
                LocalDate.now().plusDays(5), LocalDate.now(), "N");

        when(tradeValidationService.validateTrade(trade)).thenReturn(true);

        // When
        tradeProjectionService.updateTradeProjectStore(trade);

        // Then
        verify(tradeStore).insertTrade(trade);
        verify(tradeStore, never()).insertTradeException(any(), anyString());
    }

    @Test
    void testUpdateTradeProjectStore_InvalidTrade_LowerVersion() {
        // Given
        Trade trade = createSampleTrade("T1", 1, "CP-1", "B1",
                LocalDate.now().plusDays(5), LocalDate.now(), "N");

        when(tradeValidationService.validateTrade(trade)).thenReturn(false);
        when(tradeValidationService.getValidationFailureReason(trade)).thenReturn("Lower version received: 1 < 2");

        // When
        tradeProjectionService.updateTradeProjectStore(trade);

        // Then
        verify(tradeStore, never()).insertTrade(any());
        verify(tradeStore).insertTradeException(eq(trade), anyString());
    }

    @Test
    void testUpdateTradeProjectStore_InvalidTrade_PastMaturity() {
        // Given
        Trade trade = createSampleTrade("T1", 1, "CP-1", "B1",
                LocalDate.now().minusDays(1), LocalDate.now(), "N");

        when(tradeValidationService.validateTrade(trade)).thenReturn(false);
        when(tradeValidationService.getValidationFailureReason(trade)).thenReturn("Maturity date in past");

        // When
        tradeProjectionService.updateTradeProjectStore(trade);

        // Then
        verify(tradeStore, never()).insertTrade(any());
        verify(tradeStore).insertTradeException(eq(trade), anyString());
    }

    @Test
    void testValidateTrade_ValidTrade() {
        // Given
        Trade trade = createSampleTrade("T1", 1, "CP-1", "B1",
                LocalDate.now().plusDays(5), LocalDate.now(), "N");
        tradeProjectionService.updateTradeProjectStore(trade);

        when(tradeValidationService.validateTrade(trade)).thenReturn(true);

        // When
        boolean isValid = tradeProjectionService.validateTrade();

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateTrade_InvalidTrade() {
        // Given
        Trade trade = createSampleTrade("T1", 1, "CP-1", "B1",
                LocalDate.now().minusDays(1), LocalDate.now(), "N");
        tradeProjectionService.updateTradeProjectStore(trade);

        when(tradeValidationService.validateTrade(trade)).thenReturn(false);

        // When
        boolean isValid = tradeProjectionService.validateTrade();

        // Then
        assertFalse(isValid);
    }

    private Trade createSampleTrade(String tradeId, int version, String counterPartyId,
            String bookId, LocalDate maturityDate, LocalDate createdDate, String expired) {
        return new Trade(tradeId, version, counterPartyId, bookId, maturityDate, createdDate, expired,
                "req-" + tradeId);
    }
}
