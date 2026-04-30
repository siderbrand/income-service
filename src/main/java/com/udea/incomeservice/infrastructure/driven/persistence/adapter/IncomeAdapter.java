package com.udea.incomeservice.infrastructure.driven.persistence.adapter;

import com.udea.incomeservice.domain.gateway.IncomeGateway;
import com.udea.incomeservice.domain.model.Income;
import com.udea.incomeservice.infrastructure.driven.persistence.mapper.IncomeEntityMapper;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.IncomeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IncomeAdapter implements IncomeGateway {

    private final IncomeJpaRepository repository;
    private final IncomeEntityMapper mapper;

    @Override
    public Income save(Income income) {
        return mapper.toDomain(repository.save(mapper.toEntity(income)));
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
        return repository.findByUserIdAndMonth(userId, year, month)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}