package com.udea.incomeservice.infrastructure.entrypoint.rest.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class IncomeRequestDTO {

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal amount;

    @NotBlank(message = "La descripción es requerida")
    @Size(max = 255)
    private String description;

    @NotNull(message = "La fecha es requerida")
    private LocalDate date;

    @NotBlank(message = "Debes seleccionar una categoría")
    private String category;

    @NotNull(message = "El usuario es requerido")
    private Long userId;
}