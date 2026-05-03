package com.udea.incomeservice.infrastructure.driven.persistence.adapter;

import com.udea.incomeservice.domain.gateway.IncomeGateway;
import com.udea.incomeservice.domain.model.Income;
import com.udea.incomeservice.infrastructure.driven.persistence.mapper.IncomeEntityMapper;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.IncomeJpaRepository;
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
public class IncomeAdapter implements IncomeGateway {

    private final IncomeJpaRepository repository;
    private final IncomeEntityMapper mapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Income save(Income income) {
        var saved = repository.save(mapper.toEntity(income));
        entityManager.refresh(saved);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Income> findByUserId(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Income> findByUserIdAndMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return repository.findByUserIdAndDateBetween(userId, start, end)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public BigDecimal sumByUserIdAndMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return repository.sumAmountByUserIdAndDateBetween(
                userId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }
}