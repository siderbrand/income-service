package com.udea.incomeservice.infrastructure.entrypoint.rest.controller;

import com.udea.incomeservice.domain.usecase.IncomeUseCase;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.IncomeRequestDTO;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.IncomeResponseDTO;
import com.udea.incomeservice.infrastructure.entrypoint.rest.mapper.IncomeRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@Tag(name = "Incomes", description = "Gestión de ingresos financieros")
public class IncomeController {

    private final IncomeUseCase incomeUseCase;
    private final IncomeRestMapper mapper;

    @PostMapping
    @Operation(summary = "Registrar un ingreso")
    public ResponseEntity<IncomeResponseDTO> registerIncome(
            @Valid @RequestBody IncomeRequestDTO request) {
        var income = incomeUseCase.registerIncome(mapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(income));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar todos los ingresos de un usuario")
    public ResponseEntity<List<IncomeResponseDTO>> getAllIncomes(
            @PathVariable Long userId) {
        var incomes = incomeUseCase.getAllIncomes(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(incomes);
    }

    @GetMapping("/user/{userId}/monthly")
    @Operation(summary = "Listar ingresos del mes")
    public ResponseEntity<List<IncomeResponseDTO>> getMonthlyIncomes(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        var incomes = incomeUseCase.getMonthlyIncomes(userId, year, month)
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(incomes);
    }
}