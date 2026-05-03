package com.udea.incomeservice.infrastructure.driven.persistence.mapper;

import com.udea.incomeservice.domain.model.Income;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.IncomeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncomeEntityMapper {

    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "createdAt", ignore = true)
    IncomeEntity toEntity(Income income);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    Income toDomain(IncomeEntity entity);
}
