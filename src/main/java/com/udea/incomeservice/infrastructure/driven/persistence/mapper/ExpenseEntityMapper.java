package com.udea.incomeservice.infrastructure.driven.persistence.mapper;

import com.udea.incomeservice.domain.model.Expense;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.ExpenseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseEntityMapper {

    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "createdAt", ignore = true)
    ExpenseEntity toEntity(Expense expense);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    Expense toDomain(ExpenseEntity entity);
}
