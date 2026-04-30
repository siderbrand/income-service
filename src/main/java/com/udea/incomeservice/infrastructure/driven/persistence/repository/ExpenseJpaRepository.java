package com.udea.incomeservice.infrastructure.driven.persistence.repository;

import com.udea.incomeservice.infrastructure.driven.persistence.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, Long> {

    List<ExpenseEntity> findByUserId(Long userId);

    List<ExpenseEntity> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
