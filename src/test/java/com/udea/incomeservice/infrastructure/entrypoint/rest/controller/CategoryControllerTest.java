package com.udea.incomeservice.infrastructure.entrypoint.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.incomeservice.IncomeServiceApplication;
import com.udea.incomeservice.infrastructure.driven.persistence.entity.CategoryEntity;
import com.udea.incomeservice.infrastructure.driven.persistence.repository.CategoryJpaRepository;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.CategoryRequestDTO;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = IncomeServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerTest {

    private static final String BASE_URL = "/api/categories";
    private static final Long USER_ID    = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CategoryJpaRepository categoryRepository;

    private String token;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        categoryRepository.flush();
        token = "Bearer " + JwtTestUtil.generateToken(USER_ID);
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private CategoryEntity persistCategory(String name, String type) {
        return categoryRepository.saveAndFlush(
                CategoryEntity.builder()
                        .userId(USER_ID)
                        .name(name.toUpperCase())
                        .type(type)
                        .build());
    }

    @Test
    @DisplayName("CP-01-01: Crear categoría exitosamente devuelve 201")
    void cp0101_createCategory_returns201() throws Exception {
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setName("Salario");
        request.setType("INCOME");

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("SALARIO"))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    @DisplayName("CP-01-02: Categoría duplicada devuelve 409")
    void cp0102_duplicateCategory_returns409() throws Exception {
        persistCategory("Salario", "INCOME");

        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setName("Salario");
        request.setType("INCOME");

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe una categoría con ese nombre"));
    }

    @Test
    @DisplayName("CP-01-03: Tipo de categoría inválido devuelve 400")
    void cp0103_invalidCategoryType_returns400() throws Exception {
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setName("Miscelánea");
        request.setType("OTRO");

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El tipo debe ser INCOME o EXPENSE"));
    }

    @Test
    @DisplayName("CP-01-04: Campos vacíos devuelven 400")
    void cp0104_emptyFields_returns400() throws Exception {
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setName("");
        request.setType("");

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CP-01-05: Listar todas las categorías del usuario devuelve 200")
    void cp0105_getAllCategories_returns200() throws Exception {
        persistCategory("Salario", "INCOME");
        persistCategory("Arriendo", "EXPENSE");

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("CP-01-06: Filtrar categorías por tipo INCOME devuelve solo las de ese tipo")
    void cp0106_getCategoriesByType_returnsFilteredList() throws Exception {
        persistCategory("Salario", "INCOME");
        persistCategory("Freelance", "INCOME");
        persistCategory("Arriendo", "EXPENSE");

        mockMvc.perform(get(BASE_URL).param("type", "INCOME")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("CP-01-07: Petición sin token devuelve 403")
    void cp0107_requestWithoutToken_returns403() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }
}