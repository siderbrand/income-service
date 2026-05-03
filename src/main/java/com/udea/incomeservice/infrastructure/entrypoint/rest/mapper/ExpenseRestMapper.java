package com.udea.incomeservice.infrastructure.entrypoint.rest.mapper;

import com.udea.incomeservice.domain.model.Expense;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.ExpenseRequestDTO;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.ExpenseResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "userId", source = "userId")
    Expense toDomain(ExpenseRequestDTO dto, Long userId);

    @Mapping(target = "categoryName", source = "categoryName")
    ExpenseResponseDTO toResponse(Expense expense);
}
