package com.udea.incomeservice.infrastructure.entrypoint.rest.controller;

import com.udea.incomeservice.domain.exception.DomainConstants;
import com.udea.incomeservice.domain.model.MonthlyBalance;
import com.udea.incomeservice.domain.usecase.BalanceUseCase;
import com.udea.incomeservice.infrastructure.entrypoint.rest.EntryPointConstants;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.MonthlyBalanceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EntryPointConstants.BALANCE_BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Balance", description = "Balance financiero mensual")
@SecurityRequirement(name = "bearerAuth")
public class BalanceController {

    private final BalanceUseCase balanceUseCase;

    @GetMapping
    @Operation(summary = "Balance del mes actual")
    public ResponseEntity<MonthlyBalanceDTO> getCurrentBalance(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(toDTO(balanceUseCase.getMonthlyBalance(userId)));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Balance de un mes específico")
    public ResponseEntity<MonthlyBalanceDTO> getMonthlyBalance(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(toDTO(balanceUseCase.getMonthlyBalance(userId, year, month)));
    }

    private MonthlyBalanceDTO toDTO(MonthlyBalance balance) {
        String alert = MonthlyBalance.NEGATIVE.equals(balance.getStatus())
                ? DomainConstants.BALANCE_NEGATIVE_ALERT
                : null;
        return MonthlyBalanceDTO.builder()
                .totalIncomes(balance.getTotalIncomes())
                .totalExpenses(balance.getTotalExpenses())
                .balance(balance.getBalance())
                .status(balance.getStatus())
                .alert(alert)
                .build();
    }
}
