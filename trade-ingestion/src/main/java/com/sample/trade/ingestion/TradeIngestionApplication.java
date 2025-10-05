package com.sample.trade.ingestion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.sample.trade.common.service.TradeQueryService;
import com.sample.trade.common.store.TradeStore;

@SpringBootApplication
public class TradeIngestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradeIngestionApplication.class, args);
    }

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String databaseDriverClassName;

    @Bean
    public JdbcTemplate jdbcTemplate() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(databaseDriverClassName);
        dataSource.setUrl(databaseUrl);
        dataSource.setUsername(databaseUsername);
        dataSource.setPassword(databasePassword);
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public TradeStore tradeStore(JdbcTemplate jdbcTemplate) {
        return new TradeStore(jdbcTemplate);
    }

    @Bean
    public TradeQueryService tradeQueryService() {
        return new TradeQueryService();
    }
}
