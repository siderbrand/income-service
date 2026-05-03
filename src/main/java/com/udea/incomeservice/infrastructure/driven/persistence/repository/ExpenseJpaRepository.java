package com.udea.incomeservice.infrastructure.driven.persistence.repository;

import com.udea.incomeservice.infrastructure.driven.persistence.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, Long> {

    List<ExpenseEntity> findByUserId(Long userId);

    List<ExpenseEntity> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e " +
            "WHERE e.userId = :userId AND e.category.id = :categoryId " +
            "AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndCategoryIdAndDateBetween(
            Long userId, Long categoryId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e " +
            "WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
