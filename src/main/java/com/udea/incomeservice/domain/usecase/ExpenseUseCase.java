package com.udea.incomeservice.domain.usecase;

import com.udea.incomeservice.domain.exception.DomainConstants;
import com.udea.incomeservice.domain.exception.InvalidExpenseException;
import com.udea.incomeservice.domain.gateway.ExpenseGateway;
import com.udea.incomeservice.domain.model.Expense;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class ExpenseUseCase {

    private final ExpenseGateway expenseGateway;

    public Expense registerExpense(Expense expense) {
        validateExpense(expense);
        return expenseGateway.save(expense);
    }

    public List<Expense> getMonthlyExpenses(Long userId, int year, int month) {
        return expenseGateway.findByUserIdAndMonth(userId, year, month);
    }

    public List<Expense> getAllExpenses(Long userId) {
        return expenseGateway.findByUserId(userId);
    }

    private void validateExpense(Expense expense) {
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseException(DomainConstants.AMOUNT_MUST_BE_POSITIVE);
        }
        if (expense.getCategory() == null || expense.getCategory().isBlank()) {
            throw new InvalidExpenseException(DomainConstants.CATEGORY_REQUIRED);
        }
        if (expense.getDate() == null) {
            throw new InvalidExpenseException(DomainConstants.DATE_REQUIRED);
        }
        if (expense.getDate().isAfter(LocalDate.now())) {
            throw new InvalidExpenseException(DomainConstants.DATE_CANNOT_BE_FUTURE);
        }
        if (expense.getDescription() == null || expense.getDescription().isBlank()) {
            throw new InvalidExpenseException(DomainConstants.DESCRIPTION_REQUIRED);
        }
    }
}
