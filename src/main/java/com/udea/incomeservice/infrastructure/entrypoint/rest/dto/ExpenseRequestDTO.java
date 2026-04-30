package com.udea.incomeservice.infrastructure.entrypoint.rest.dto;

import com.udea.incomeservice.infrastructure.entrypoint.rest.EntryPointConstants;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExpenseRequestDTO {

    @NotNull(message = EntryPointConstants.AMOUNT_FIELD_REQUIRED)
    @DecimalMin(value = EntryPointConstants.AMOUNT_MIN, message = EntryPointConstants.AMOUNT_MUST_BE_POSITIVE)
    private BigDecimal amount;

    @NotBlank(message = EntryPointConstants.DESCRIPTION_REQUIRED)
    @Size(max = EntryPointConstants.DESCRIPTION_MAX_LENGTH)
    private String description;

    @NotNull(message = EntryPointConstants.DATE_REQUIRED)
    private LocalDate date;

    @NotBlank(message = EntryPointConstants.CATEGORY_REQUIRED)
    private String category;
}
