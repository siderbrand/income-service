package com.udea.incomeservice.infrastructure.driven.persistence.mapper;

import com.udea.incomeservice.domain.model.Income;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.IncomeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IncomeEntityMapper {

    IncomeEntity toEntity(Income income);

    Income toDomain(IncomeEntity entity);
}
