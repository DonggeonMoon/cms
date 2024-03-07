package com.dgmoonlabs.cms.global.config;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {"com.dgmoonlabs.cms"})
@RequiredArgsConstructor
public class MybatisConfig {
    private final DataSource dataSource;

    @Bean("transactionManager")
    @Primary
    public PlatformTransactionManager mybatisTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}
