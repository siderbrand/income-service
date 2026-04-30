package com.udea.incomeservice.infrastructure.entrypoint.rest.mapper;

import com.udea.incomeservice.domain.model.Income;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.IncomeRequestDTO;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.IncomeResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncomeRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "userId", source = "userId")
    Income toDomain(IncomeRequestDTO dto, Long userId);

    IncomeResponseDTO toResponse(Income income);
}
