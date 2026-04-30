package com.udea.incomeservice.infrastructure.driven.persistence.adapter;

import com.udea.incomeservice.domain.gateway.ExpenseGateway;
import com.udea.incomeservice.domain.model.Expense;
import com.udea.incomeservice.infrastructure.driven.persistence.mapper.ExpenseEntityMapper;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.ExpenseJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpenseAdapter implements ExpenseGateway {

    private final ExpenseJpaRepository repository;
    private final ExpenseEntityMapper mapper;

    @Override
    public Expense save(Expense expense) {
        return mapper.toDomain(repository.save(mapper.toEntity(expense)));
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
}
