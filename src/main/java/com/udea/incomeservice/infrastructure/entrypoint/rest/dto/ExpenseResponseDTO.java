package com.udea.incomeservice.infrastructure.entrypoint.rest.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ExpenseResponseDTO {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private String category;
    private LocalDateTime createdAt;
}
