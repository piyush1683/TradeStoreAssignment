package com.sample.trade.common.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradeStoreTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private TradeStore tradeStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tradeStore = new TradeStore(jdbcTemplate);
    }

    @Test
    void testTradeStoreCreation() {
        assertNotNull(tradeStore);
    }

    @Test
    void testGetLatestVersion() {
        String tradeId = "T1";
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(tradeId)))
                .thenReturn(1);

        Integer version = tradeStore.getLatestVersion(tradeId);
        assertEquals(1, version);
    }
}
