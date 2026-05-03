package com.udea.incomeservice.infrastructure.driven.persistence.repository;

import com.udea.incomeservice.infrastructure.driven.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByUserIdAndNameIgnoreCaseAndType(Long userId, String name, String type);
    List<CategoryEntity> findByUserIdAndType(Long userId, String type);
    List<CategoryEntity> findByUserId(Long userId);
}
