package com.dgmoonlabs.cms.global.config;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = {"com.dgmoonlabs.cms"})
@RequiredArgsConstructor
public class MybatisConfig {
}
