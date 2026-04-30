package com.udea.incomeservice.infrastructure.entrypoint.rest.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class IncomeResponseDTO {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private String category;
}