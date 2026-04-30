package com.udea.incomeservice.infrastructure.driven.persistence.mapper;

import com.udea.incomeservice.domain.model.Income;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.IncomeEntity;
import org.springframework.stereotype.Component;

@Component
public class IncomeEntityMapper {

    public IncomeEntity toEntity(Income income) {
        return IncomeEntity.builder()
                .id(income.getId())
                .userId(income.getUserId())
                .amount(income.getAmount())
                .description(income.getDescription())
                .date(income.getDate())
                .category(income.getCategory())
                .build();
    }

    public Income toDomain(IncomeEntity entity) {
        return Income.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .date(entity.getDate())
                .category(entity.getCategory())
                .build();
    }
}