package com.udea.incomeservice.domain.gateway;

import com.udea.incomeservice.domain.model.Category;

import java.util.List;

public interface CategoryGateway {
    Category save(Category category);
    boolean existsByUserIdAndNameAndType(Long userId, String name, String type);
    List<Category> findByUserIdAndType(Long userId, String type);
    List<Category> findByUserId(Long userId);
    boolean existsById(Long id);
}
