package com.udea.incomeservice.infrastructure.driven.persistence.repository;

import com.udea.incomeservice.infrastructure.driven.persistence.entity.IncomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IncomeJpaRepository extends JpaRepository<IncomeEntity, Long> {

    List<IncomeEntity> findByUserId(Long userId);

    @Query("SELECT i FROM IncomeEntity i WHERE i.userId = :userId " +
            "AND YEAR(i.date) = :year AND MONTH(i.date) = :month")
    List<IncomeEntity> findByUserIdAndMonth(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month);
}