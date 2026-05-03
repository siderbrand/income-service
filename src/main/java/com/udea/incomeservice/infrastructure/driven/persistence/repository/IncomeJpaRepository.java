package com.udea.incomeservice.infrastructure.driven.persistence.repository;

import com.udea.incomeservice.infrastructure.driven.persistence.entity.IncomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeJpaRepository extends JpaRepository<IncomeEntity, Long> {

    List<IncomeEntity> findByUserId(Long userId);

    List<IncomeEntity> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM IncomeEntity i " +
            "WHERE i.userId = :userId AND i.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
