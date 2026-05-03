package com.udea.incomeservice.infrastructure.driven.persistence.adapter;

import com.udea.incomeservice.domain.gateway.ExpenseGateway;
import com.udea.incomeservice.domain.model.Expense;
import com.udea.incomeservice.infrastructure.driven.persistence.mapper.ExpenseEntityMapper;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.ExpenseJpaRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpenseAdapter implements ExpenseGateway {

    private final ExpenseJpaRepository repository;
    private final ExpenseEntityMapper mapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Expense save(Expense expense) {
        var saved = repository.save(mapper.toEntity(expense));
        entityManager.refresh(saved);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Expense> findByUserId(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Expense> findByUserIdAndMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return repository.findByUserIdAndDateBetween(userId, start, end)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public BigDecimal sumByUserIdAndCategoryIdAndMonth(Long userId, Long categoryId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return repository.sumAmountByUserIdAndCategoryIdAndDateBetween(
                userId, categoryId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    @Override
    public BigDecimal sumByUserIdAndMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return repository.sumAmountByUserIdAndDateBetween(
                userId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }
}
