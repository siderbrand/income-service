package com.udea.incomeservice.application.config;

import com.udea.incomeservice.domain.gateway.BudgetGateway;
import com.udea.incomeservice.domain.gateway.CategoryGateway;
import com.udea.incomeservice.domain.gateway.ExpenseGateway;
import com.udea.incomeservice.domain.gateway.IncomeGateway;
import com.udea.incomeservice.domain.usecase.BalanceUseCase;
import com.udea.incomeservice.domain.usecase.BudgetUseCase;
import com.udea.incomeservice.domain.usecase.CategoryUseCase;
import com.udea.incomeservice.domain.usecase.ExpenseUseCase;
import com.udea.incomeservice.domain.usecase.IncomeUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IncomeBeanConfig {

    @Bean
    public IncomeUseCase incomeUseCase(IncomeGateway incomeGateway, CategoryGateway categoryGateway) {
        return new IncomeUseCase(incomeGateway, categoryGateway);
    }

    @Bean
    public ExpenseUseCase expenseUseCase(ExpenseGateway expenseGateway, CategoryGateway categoryGateway) {
        return new ExpenseUseCase(expenseGateway, categoryGateway);
    }

    @Bean
    public CategoryUseCase categoryUseCase(CategoryGateway categoryGateway) {
        return new CategoryUseCase(categoryGateway);
    }

    @Bean
    public BudgetUseCase budgetUseCase(BudgetGateway budgetGateway, ExpenseGateway expenseGateway, CategoryGateway categoryGateway) {
        return new BudgetUseCase(budgetGateway, expenseGateway, categoryGateway);
    }

    @Bean
    public BalanceUseCase balanceUseCase(IncomeGateway incomeGateway, ExpenseGateway expenseGateway) {
        return new BalanceUseCase(incomeGateway, expenseGateway);
    }
}
