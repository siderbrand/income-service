package com.udea.incomeservice.domain.usecase;

import com.udea.incomeservice.domain.exception.DomainConstants;
import com.udea.incomeservice.domain.exception.InvalidIncomeException;
import com.udea.incomeservice.domain.gateway.CategoryGateway;
import com.udea.incomeservice.domain.gateway.IncomeGateway;
import com.udea.incomeservice.domain.model.Income;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class IncomeUseCase {

    private final IncomeGateway incomeGateway;
    private final CategoryGateway categoryGateway;

    public Income registerIncome(Income income) {
        validateIncome(income);
        return incomeGateway.save(income);
    }

    public List<Income> getMonthlyIncomes(Long userId, int year, int month) {
        return incomeGateway.findByUserIdAndMonth(userId, year, month);
    }

    public List<Income> getAllIncomes(Long userId) {
        return incomeGateway.findByUserId(userId);
    }

    private void validateIncome(Income income) {
        if (income.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidIncomeException(DomainConstants.AMOUNT_MUST_BE_POSITIVE);
        }
        if (!categoryGateway.existsById(income.getCategoryId())) {
            throw new InvalidIncomeException(DomainConstants.CATEGORY_NOT_FOUND);
        }
    }
}
