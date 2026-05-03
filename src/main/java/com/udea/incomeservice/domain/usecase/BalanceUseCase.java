package com.udea.incomeservice.domain.usecase;

import com.udea.incomeservice.domain.gateway.ExpenseGateway;
import com.udea.incomeservice.domain.gateway.IncomeGateway;
import com.udea.incomeservice.domain.model.MonthlyBalance;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
public class BalanceUseCase {

    private final IncomeGateway incomeGateway;
    private final ExpenseGateway expenseGateway;

    public MonthlyBalance getMonthlyBalance(Long userId) {
        LocalDate now = LocalDate.now();
        return getMonthlyBalance(userId, now.getYear(), now.getMonthValue());
    }

    public MonthlyBalance getMonthlyBalance(Long userId, int year, int month) {
        BigDecimal totalIncomes = incomeGateway.sumByUserIdAndMonth(userId, year, month);
        BigDecimal totalExpenses = expenseGateway.sumByUserIdAndMonth(userId, year, month);
        BigDecimal balance = totalIncomes.subtract(totalExpenses);

        String status;
        int cmp = balance.compareTo(BigDecimal.ZERO);
        if (cmp > 0) {
            status = MonthlyBalance.POSITIVE;
        } else if (cmp < 0) {
            status = MonthlyBalance.NEGATIVE;
        } else {
            status = MonthlyBalance.ZERO;
        }

        return MonthlyBalance.builder()
                .totalIncomes(totalIncomes)
                .totalExpenses(totalExpenses)
                .balance(balance)
                .status(status)
                .build();
    }
}
