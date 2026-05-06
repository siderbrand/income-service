package com.udea.incomeservice.infrastructure.entrypoint.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.incomeservice.IncomeServiceApplication;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.CategoryEntity;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.CategoryJpaRepository;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.IncomeJpaRepository;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.IncomeRequestDTO;
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
class IncomeControllerTest {

    private static final String BASE_URL = "/api/incomes";
    private static final Long   USER_ID  = 10L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CategoryJpaRepository categoryRepository;
    @Autowired private IncomeJpaRepository incomeRepository;

    private String token;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        incomeRepository.deleteAll();
        categoryRepository.deleteAll();
        categoryRepository.flush();

        token = "Bearer " + JwtTestUtil.generateToken(USER_ID);

        CategoryEntity category = categoryRepository.saveAndFlush(
                CategoryEntity.builder()
                        .userId(USER_ID)
                        .name("SALARIO")
                        .type("INCOME")
                        .build());
        categoryId = category.getId();
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private IncomeRequestDTO validRequest() {
        IncomeRequestDTO dto = new IncomeRequestDTO();
        dto.setAmount(new BigDecimal("2500.00"));
        dto.setDescription("Salario mensual");
        dto.setDate(LocalDate.now());
        dto.setCategoryId(categoryId);
        return dto;
    }

    @Test
    @DisplayName("CP-02-01: Registrar ingreso exitosamente devuelve 201")
    void cp0201_registerIncome_returns201() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(2500.00))
                .andExpect(jsonPath("$.description").value("Salario mensual"))
                .andExpect(jsonPath("$.categoryName").isNotEmpty());
    }

    @Test
    @DisplayName("CP-02-02: Ingreso con categoría inexistente devuelve 400")
    void cp0202_incomeWithNonExistentCategory_returns400() throws Exception {
        IncomeRequestDTO request = validRequest();
        request.setCategoryId(999L);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La categoría seleccionada no existe"));
    }

    @Test
    @DisplayName("CP-02-03: Ingreso con monto cero devuelve 400")
    void cp0203_incomeWithZeroAmount_returns400() throws Exception {
        IncomeRequestDTO request = validRequest();
        request.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CP-02-04: Ingreso sin descripción devuelve 400")
    void cp0204_incomeWithoutDescription_returns400() throws Exception {
        IncomeRequestDTO request = validRequest();
        request.setDescription("");

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La descripción es requerida"));
    }

    @Test
    @DisplayName("CP-02-05: Listar ingresos de un usuario devuelve 200 con lista correcta")
    void cp0205_getAllIncomes_returns200() throws Exception {
        // Registrar dos ingresos primero
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(validRequest())));

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(validRequest())));

        mockMvc.perform(get(BASE_URL + "/user/{userId}", USER_ID)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("CP-02-06: Listar ingresos mensuales filtra correctamente por año y mes")
    void cp0206_getMonthlyIncomes_returnsFilteredByMonth() throws Exception {
        // Ingreso en el mes actual
        mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(validRequest())));

        // Ingreso en un mes anterior
        IncomeRequestDTO old = validRequest();
        old.setDate(LocalDate.now().minusMonths(2));
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

    @Test
    @DisplayName("CP-02-07: Petición sin token devuelve 403")
    void cp0207_requestWithoutToken_returns403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isForbidden());
    }
}