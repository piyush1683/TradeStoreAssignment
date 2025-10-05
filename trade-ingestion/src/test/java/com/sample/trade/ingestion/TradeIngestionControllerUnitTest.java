package com.sample.trade.ingestion;

import com.sample.trade.common.model.Trade;
import com.sample.trade.common.model.TradeException;
import com.sample.trade.common.service.TradeQueryService;
import com.sample.trade.ingestion.api.TradeIngestionController;
import com.sample.trade.ingestion.service.KafkaTradeIngestionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TradeIngestionControllerUnitTest {

        @Mock
        private KafkaTradeIngestionService kafkaTradeIngestionService;

        @Mock
        private TradeQueryService tradeQueryService;

        private MockMvc mockMvc;
        private TradeIngestionController controller;

        @BeforeEach
        void setUp() {
                controller = new TradeIngestionController(kafkaTradeIngestionService, tradeQueryService);
                mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        }

        @Test
        void testCreateSingleTrade_ReturnsAccepted() throws Exception {
                mockMvc.perform(post("/api/trade")
                                .contentType("application/json")
                                .content("{\"tradeId\":\"T1\",\"version\":1,\"counterPartyId\":\"CP1\",\"bookId\":\"B1\",\"maturityDate\":\"2024-12-31\",\"createdDate\":\"2024-01-01\",\"expired\":\"N\",\"requestId\":\"req-1\"}"))
                                .andExpect(status().isAccepted());
        }

        @Test
        void testGetNotifications_WithRequestId_ReturnsOk() throws Exception {
                when(tradeQueryService.getTradeExceptionsByRequestId("req-123")).thenReturn(List.of());

                mockMvc.perform(get("/api/notifications")
                                .param("requestId", "req-123"))
                                .andExpect(status().isOk());
        }

        @Test
        void testGetNotifications_WithoutParameters_ReturnsBadRequest() throws Exception {
                mockMvc.perform(get("/api/notifications"))
                                .andExpect(status().isBadRequest());
        }
}