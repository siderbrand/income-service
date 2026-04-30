package com.udea.incomeservice.infrastructure.driven.persistence.mapper;

import com.udea.incomeservice.domain.model.Expense;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.ExpenseEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseEntityMapper {

    ExpenseEntity toEntity(Expense expense);

    Expense toDomain(ExpenseEntity entity);
}
