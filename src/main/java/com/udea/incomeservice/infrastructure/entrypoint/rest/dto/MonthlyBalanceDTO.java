package com.udea.incomeservice.infrastructure.entrypoint.rest.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MonthlyBalanceDTO {
    private BigDecimal totalIncomes;
    private BigDecimal totalExpenses;
    private BigDecimal balance;
    private String status;
    private String alert;
}
