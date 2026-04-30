package com.udea.incomeservice.domain.gateway;

import com.udea.incomeservice.domain.model.Expense;

import java.util.List;

public interface ExpenseGateway {
    Expense save(Expense expense);
    List<Expense> findByUserId(Long userId);
    List<Expense> findByUserIdAndMonth(Long userId, int year, int month);
}
