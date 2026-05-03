package com.udea.incomeservice.domain.gateway;

import com.udea.incomeservice.domain.model.Income;

import java.math.BigDecimal;
import java.util.List;

public interface IncomeGateway {
    Income save(Income income);
    List<Income> findByUserId(Long userId);
    List<Income> findByUserIdAndMonth(Long userId, int year, int month);
    BigDecimal sumByUserIdAndMonth(Long userId, int year, int month);
}