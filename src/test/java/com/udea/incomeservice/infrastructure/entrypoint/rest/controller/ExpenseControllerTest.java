package com.udea.incomeservice.infrastructure.entrypoint.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.incomeservice.IncomeServiceApplication;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.CategoryEntity;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.BudgetJpaRepository;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.CategoryJpaRepository;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.ExpenseJpaRepository;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.BudgetRequestDTO;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.ExpenseRequestDTO;
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
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = IncomeServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExpenseControllerTest {

    private static final String BASE_URL    = "/api/expenses";
    private static final String BUDGET_URL  = "/api/budgets";
    private static final Long   USER_ID     = 20L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CategoryJpaRepository categoryRepository;
    @Autowired private ExpenseJpaRepository expenseRepository;
    @Autowired private BudgetJpaRepository budgetRepository;

    private String token;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        budgetRepository.deleteAll();
        expenseRepository.deleteAll();
        categoryRepository.deleteAll();
        categoryRepository.flush();

        token = "Bearer " + JwtTestUtil.generateToken(USER_ID);

        CategoryEntity category = categoryRepository.saveAndFlush(
                CategoryEntity.builder()
                        .userId(USER_ID)
                        .name("MERCADO")
                        .type("EXPENSE")
                        .build());
        categoryId = category.getId();
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private ExpenseRequestDTO validRequest() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO();
        dto.setAmount(new BigDecimal("150.00"));
        dto.setDescription("Compras semanales");
        dto.setDate(LocalDate.now());
        dto.setCategoryId(categoryId);
        return dto;
    }

    @Test
    @DisplayName("CP-03-01: Registrar gasto exitosamente devuelve 201")
    void cp0301_registerExpense_returns201() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.categoryName").isNotEmpty());
    }

    @Test
    @DisplayName("CP-03-02: Gasto con categoría inexistente devuelve 400")
    void cp0302_expenseWithNonExistentCategory_returns400() throws Exception {
        ExpenseRequestDTO request = validRequest();
        request.setCategoryId(999L);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La categoría seleccionada no existe"));
    }

    @Test
    @DisplayName("CP-03-03: Gasto sin descripción devuelve 400")
    void cp0303_expenseWithoutDescription_returns400() throws Exception {
        ExpenseRequestDTO request = validRequest();
        request.setDescription("  ");

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La descripción es requerida"));
    }

    @Test
    @DisplayName("CP-03-04: Gasto que supera el 80% del presupuesto incluye alerta WARNING en respuesta")
    void cp0304_expenseNearBudgetLimit_returnsWarningAlert() throws Exception {
        // Crear presupuesto de $200 para la categoría
        BudgetRequestDTO budget = new BudgetRequestDTO();
        budget.setCategoryId(categoryId);
        budget.setMaxAmount(new BigDecimal("200.00"));
        mockMvc.perform(post(BUDGET_URL)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(budget)));

        // Registrar gasto de $170 (85% del presupuesto → WARNING)
        ExpenseRequestDTO request = validRequest();
        request.setAmount(new BigDecimal("170.00"));

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.budgetAlert").isNotEmpty());
    }

    @Test
    @DisplayName("CP-03-05: Gasto que supera el 100% del presupuesto incluye alerta EXCEEDED en respuesta")
    void cp0305_expenseExceedsBudget_returnsExceededAlert() throws Exception {
        // Crear presupuesto de $100
        BudgetRequestDTO budget = new BudgetRequestDTO();
        budget.setCategoryId(categoryId);
        budget.setMaxAmount(new BigDecimal("100.00"));
        mockMvc.perform(post(BUDGET_URL)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(budget)));

        // Registrar gasto de $150 (150% → EXCEEDED)
        ExpenseRequestDTO request = validRequest();
        request.setAmount(new BigDecimal("150.00"));

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.budgetAlert").value("Presupuesto agotado"));
    }

    @Test
    @DisplayName("CP-03-06: Listar gastos de un usuario devuelve 200")
    void cp0306_getAllExpenses_returns200() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(validRequest())));

        mockMvc.perform(get(BASE_URL + "/user/{userId}", USER_ID)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("CP-03-07: Listar gastos mensuales filtra correctamente por año y mes")
    void cp0307_getMonthlyExpenses_returnsFilteredByMonth() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(validRequest())));

        ExpenseRequestDTO old = validRequest();
        old.setDate(LocalDate.now().minusMonths(3));
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(old)));

        int year  = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        mockMvc.perform(get(BASE_URL + "/user/{userId}/monthly", USER_ID)
                        .param("year",  String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}