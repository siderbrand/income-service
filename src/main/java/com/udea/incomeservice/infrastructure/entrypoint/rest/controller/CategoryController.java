package com.udea.incomeservice.infrastructure.entrypoint.rest.controller;

import com.udea.incomeservice.domain.usecase.CategoryUseCase;
import com.udea.incomeservice.infrastructure.entrypoint.rest.EntryPointConstants;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.CategoryRequestDTO;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.CategoryResponseDTO;
import com.udea.incomeservice.infrastructure.entrypoint.rest.mapper.CategoryRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(EntryPointConstants.CATEGORY_BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Gestión de categorías personalizadas")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;
    private final CategoryRestMapper mapper;

    @PostMapping
    @Operation(summary = "Crear una categoría personalizada")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryRequestDTO request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        var category = categoryUseCase.createCategory(mapper.toDomain(request, userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(category));
    }

    @GetMapping
    @Operation(summary = "Listar categorías del usuario (opcional filtrar por tipo)")
    public ResponseEntity<List<CategoryResponseDTO>> getCategories(
            @RequestParam(required = false) String type,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        var categories = (type != null
                ? categoryUseCase.getCategoriesByType(userId, type)
                : categoryUseCase.getAllCategories(userId))
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(categories);
    }
}
