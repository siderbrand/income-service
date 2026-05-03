package com.udea.incomeservice.infrastructure.driven.persistence.mapper;

import com.udea.incomeservice.domain.model.Category;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.CategoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryEntityMapper {
    CategoryEntity toEntity(Category category);
    Category toDomain(CategoryEntity entity);
}
