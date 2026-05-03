package com.udea.incomeservice.domain.gateway;

import com.udea.incomeservice.domain.model.Expense;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseGateway {
    Expense save(Expense expense);
    List<Expense> findByUserId(Long userId);
    List<Expense> findByUserIdAndMonth(Long userId, int year, int month);
    BigDecimal sumByUserIdAndCategoryIdAndMonth(Long userId, Long categoryId, int year, int month);
    BigDecimal sumByUserIdAndMonth(Long userId, int year, int month);
}
