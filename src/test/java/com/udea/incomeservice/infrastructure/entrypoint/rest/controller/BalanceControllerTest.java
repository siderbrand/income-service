package com.udea.incomeservice.infrastructure.entrypoint.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.incomeservice.IncomeServiceApplication;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.CategoryEntity;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.ExpenseEntity;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.IncomeEntity;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.CategoryJpaRepository;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.ExpenseJpaRepository;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.IncomeJpaRepository;
import com.udea.incomeservice.util.JwtTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = IncomeServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BalanceControllerTest {

    private static final String BASE_URL = "/api/balance";
    private static final Long   USER_ID  = 40L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CategoryJpaRepository categoryRepository;
    @Autowired private IncomeJpaRepository incomeRepository;
    @Autowired private ExpenseJpaRepository expenseRepository;

    private String token;
    private CategoryEntity category;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        incomeRepository.deleteAll();
        categoryRepository.deleteAll();
        categoryRepository.flush();

        token = "Bearer " + JwtTestUtil.generateToken(USER_ID);

        category = categoryRepository.saveAndFlush(
                CategoryEntity.builder()
                        .userId(USER_ID)
                        .name("GENERAL")
                        .type("INCOME")
                        .build());
    }

    private void persistIncome(BigDecimal amount, LocalDate date) {
        incomeRepository.saveAndFlush(IncomeEntity.builder()
                .userId(USER_ID)
                .amount(amount)
                .description("Ingreso de prueba")
                .date(date)
                .category(category)
                .build());
    }

    private void persistExpense(BigDecimal amount, LocalDate date) {
        expenseRepository.saveAndFlush(ExpenseEntity.builder()
                .userId(USER_ID)
                .amount(amount)
                .description("Gasto de prueba")
                .date(date)
                .category(category)
                .build());
    }

    @Test
    @DisplayName("CP-05-01: Balance del mes actual sin movimientos devuelve status ZERO")
    void cp0501_balanceWithNoMovements_returnsZeroStatus() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncomes").value(0))
                .andExpect(jsonPath("$.totalExpenses").value(0))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @DisplayName("CP-05-02: Balance positivo cuando ingresos superan gastos")
    void cp0502_balanceIsPositive_whenIncomesExceedExpenses() throws Exception {
        LocalDate now = LocalDate.now();
        persistIncome(new BigDecimal("3000.00"), now);
        persistExpense(new BigDecimal("1000.00"), now);

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncomes").value(3000.00))
                .andExpect(jsonPath("$.totalExpenses").value(1000.00))
                .andExpect(jsonPath("$.balance").value(2000.00))
                .andExpect(jsonPath("$.alert").doesNotExist());
    }

    @Test
    @DisplayName("CP-05-03: Balance negativo incluye alerta cuando gastos superan ingresos")
    void cp0503_balanceIsNegative_includesAlert() throws Exception {
        LocalDate now = LocalDate.now();
        persistIncome(new BigDecimal("500.00"), now);
        persistExpense(new BigDecimal("800.00"), now);

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(-300.00))
                .andExpect(jsonPath("$.status").value("NEGATIVE"))
                .andExpect(jsonPath("$.alert").value("Tus gastos superan tus ingresos este mes"));
    }

    @Test
    @DisplayName("CP-05-04: Balance de mes específico filtra movimientos correctamente")
    void cp0504_monthlyBalance_filtersCorrectlyByYearAndMonth() throws Exception {
        LocalDate thisMonth = LocalDate.now();
        LocalDate lastMonth = LocalDate.now().minusMonths(1);

        persistIncome(new BigDecimal("2000.00"), thisMonth);
        persistIncome(new BigDecimal("1500.00"), lastMonth); // no debe contar

        mockMvc.perform(get(BASE_URL + "/monthly")
                        .param("year",  String.valueOf(thisMonth.getYear()))
                        .param("month", String.valueOf(thisMonth.getMonthValue()))
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncomes").value(2000.00));
    }

    @Test
    @DisplayName("CP-05-05: Petición sin token devuelve 403")
    void cp0505_requestWithoutToken_returns403() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }
}