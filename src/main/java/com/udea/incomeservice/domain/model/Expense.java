package com.udea.incomeservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private String category;
    private LocalDateTime createdAt;
}
