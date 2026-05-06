package com.udea.incomeservice.infrastructure.entrypoint.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.incomeservice.IncomeServiceApplication;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.CategoryEntity;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.BudgetJpaRepository;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.CategoryJpaRepository;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.ExpenseJpaRepository;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.BudgetRequestDTO;
import com.udea.incomeservice.util.JwtTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = IncomeServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BudgetControllerTest {

    private static final String BASE_URL = "/api/budgets";
    private static final Long   USER_ID  = 30L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CategoryJpaRepository categoryRepository;
    @Autowired private BudgetJpaRepository budgetRepository;
    @Autowired private ExpenseJpaRepository expenseRepository;

    private String token;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        budgetRepository.deleteAll();
        categoryRepository.deleteAll();
        categoryRepository.flush();

        token = "Bearer " + JwtTestUtil.generateToken(USER_ID);

        CategoryEntity category = categoryRepository.saveAndFlush(
                CategoryEntity.builder()
                        .userId(USER_ID)
                        .name("TRANSPORTE")
                        .type("EXPENSE")
                        .build());
        categoryId = category.getId();
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private BudgetRequestDTO validRequest() {
        BudgetRequestDTO dto = new BudgetRequestDTO();
        dto.setCategoryId(categoryId);
        dto.setMaxAmount(new BigDecimal("500.00"));
        return dto;
    }

    @Test
    @DisplayName("CP-04-01: Crear presupuesto exitosamente devuelve 201 con estado OK")
    void cp0401_createBudget_returns201WithOkStatus() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maxAmount").value(500.00))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.spentAmount").value(0));
    }

    @Test
    @DisplayName("CP-04-02: Presupuesto duplicado para misma categoría devuelve 409")
    void cp0402_duplicateBudget_returns409() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(validRequest())));

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe un presupuesto para esta categoría"));
    }

    @Test
    @DisplayName("CP-04-03: Crear presupuesto con categoría inexistente devuelve 400")
    void cp0403_budgetWithNonExistentCategory_returns400() throws Exception {
        BudgetRequestDTO request = validRequest();
        request.setCategoryId(999L);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La categoría seleccionada no existe"));
    }

    @Test
    @DisplayName("CP-04-04: Crear presupuesto con monto cero devuelve 400")
    void cp0404_budgetWithZeroAmount_returns400() throws Exception {
        BudgetRequestDTO request = validRequest();
        request.setMaxAmount(BigDecimal.ZERO);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CP-04-05: Consultar estado de presupuestos devuelve lista con estado correcto")
    void cp0405_getBudgetStatuses_returnsListWithStatus() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(validRequest())));

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("OK"))
                .andExpect(jsonPath("$[0].categoryName").value("TRANSPORTE"));
    }

    @Test
    @DisplayName("CP-04-06: Petición sin token devuelve 403")
    void cp0406_requestWithoutToken_returns403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isForbidden());
    }
}