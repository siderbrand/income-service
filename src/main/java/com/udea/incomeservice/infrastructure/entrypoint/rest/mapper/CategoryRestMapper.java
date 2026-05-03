package com.udea.incomeservice.infrastructure.entrypoint.rest.mapper;

import com.udea.incomeservice.domain.model.Category;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.CategoryRequestDTO;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.CategoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "userId")
    Category toDomain(CategoryRequestDTO dto, Long userId);

    CategoryResponseDTO toResponse(Category category);
}
