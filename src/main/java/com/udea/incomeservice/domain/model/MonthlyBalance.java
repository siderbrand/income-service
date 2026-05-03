package com.udea.incomeservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBalance {
    private BigDecimal totalIncomes;
    private BigDecimal totalExpenses;
    private BigDecimal balance;
    private String status;

    public static final String POSITIVE = "POSITIVE";
    public static final String NEGATIVE = "NEGATIVE";
    public static final String ZERO = "ZERO";
}
