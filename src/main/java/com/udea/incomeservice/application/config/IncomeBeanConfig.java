package com.udea.incomeservice.application.config;

import com.udea.incomeservice.domain.gateway.IncomeGateway;
import com.udea.incomeservice.domain.usecase.IncomeUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IncomeBeanConfig {

    @Bean
    public IncomeUseCase incomeUseCase(IncomeGateway incomeGateway) {
        return new IncomeUseCase(incomeGateway);
    }
}