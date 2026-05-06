package com.udea.incomeservice.domain.usecase;

import com.udea.incomeservice.domain.exception.DomainConstants;
import com.udea.incomeservice.domain.exception.DuplicateCategoryException;
import com.udea.incomeservice.domain.exception.InvalidIncomeException;
import com.udea.incomeservice.domain.gateway.CategoryGateway;
import com.udea.incomeservice.domain.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CP-05-EXT: Casos de prueba complementarios para CategoryUseCase.
 *
 * Cubren: normalización de nombre/tipo, aislamiento por userId,
 * tipos mixtos de casing y comportamiento con listas vacías.
 *
 * HU relacionada: HU-05 – Clasificación por Categorías
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CP-05-EXT: Casos límite y cobertura adicional – CategoryUseCase")
class CategoryUseCaseEdgeCaseTest {

    @Mock
    private CategoryGateway categoryGateway;

    @InjectMocks
    private CategoryUseCase categoryUseCase;

    private Category validCategory;

    @BeforeEach
    void setUp() {
        validCategory = Category.builder()
                .userId(1L)
                .name("Transporte")
                .type("EXPENSE")
                .build();
    }

    // ─────────────────────────────────────────────
    // CP-05-EXT-01: Nombre solo espacios – después del trim queda ""
    // El gateway recibe una cadena vacía como nombre
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-EXT-01: Nombre con solo espacios queda vacío tras el trim – el gateway recibe nombre vacío")
    void CP_05_EXT_01_createCategory_nameOnlySpaces_trimResultsInEmptyName() {
        // Arrange – nombre solo con espacios; después de trim+toUpperCase → ""
        Category whitespaceNameCategory = Category.builder()
                .userId(1L)
                .name("     ")
                .type("EXPENSE")
                .build();

        // El gateway reporta que no existe una categoría con nombre ""
        when(categoryGateway.existsByUserIdAndNameAndType(1L, "", "EXPENSE")).thenReturn(false);
        when(categoryGateway.save(any(Category.class))).thenReturn(
                Category.builder().id(5L).userId(1L).name("").type("EXPENSE").build());

        // Act
        categoryUseCase.createCategory(whitespaceNameCategory);

        // Assert – verificamos que el gateway recibió el nombre ya procesado (vacío)
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryGateway).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("");
    }

    // ─────────────────────────────────────────────
    // CP-05-EXT-02: Tipo "expense" en minúscula → normalizado a "EXPENSE"
    // Verifica que toUpperCase() aplica al tipo también
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-EXT-02: Tipo 'expense' (minúscula) es normalizado a 'EXPENSE' antes de validar")
    void CP_05_EXT_02_createCategory_lowercaseExpenseType_normalizedToUpperCase() {
        // Arrange
        Category categoryWithLowerType = Category.builder()
                .userId(1L)
                .name("Mercado")
                .type("expense")   // minúscula
                .build();

        when(categoryGateway.existsByUserIdAndNameAndType(1L, "MERCADO", "EXPENSE")).thenReturn(false);
        when(categoryGateway.save(any(Category.class))).thenReturn(
                Category.builder().id(3L).userId(1L).name("MERCADO").type("EXPENSE").build());

        // Act
        Category result = categoryUseCase.createCategory(categoryWithLowerType);

        // Assert
        assertThat(result.getType()).isEqualTo("EXPENSE");
        verify(categoryGateway).existsByUserIdAndNameAndType(1L, "MERCADO", "EXPENSE");
    }

    // ─────────────────────────────────────────────
    // CP-05-EXT-03: Mismo nombre, distinto userId → NO es duplicado
    // La unicidad aplica por (userId + name + type), no solo por nombre
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-EXT-03: Mismo nombre para distinto userId no genera duplicado")
    void CP_05_EXT_03_createCategory_sameNameDifferentUser_isNotDuplicate() {
        // Arrange – usuario 2 crea "TRANSPORTE" que ya existe para usuario 1
        Category anotherUserCategory = Category.builder()
                .userId(2L)          // userId diferente
                .name("Transporte")
                .type("EXPENSE")
                .build();

        when(categoryGateway.existsByUserIdAndNameAndType(2L, "TRANSPORTE", "EXPENSE")).thenReturn(false);
        when(categoryGateway.save(any(Category.class))).thenReturn(
                Category.builder().id(10L).userId(2L).name("TRANSPORTE").type("EXPENSE").build());

        // Act
        Category result = categoryUseCase.createCategory(anotherUserCategory);

        // Assert – debe guardarse sin lanzar excepción
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(2L);
        verify(categoryGateway).existsByUserIdAndNameAndType(2L, "TRANSPORTE", "EXPENSE");
        verify(categoryGateway, never()).existsByUserIdAndNameAndType(eq(1L), any(), any());
    }

    // ─────────────────────────────────────────────
    // CP-05-EXT-04: Tipo "income" en minúscula → normalizado, categoría válida
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-EXT-04: Tipo 'income' (minúscula) es aceptado tras normalización")
    void CP_05_EXT_04_createCategory_lowercaseIncomeType_isValid() {
        // Arrange
        Category incomeCategory = Category.builder()
                .userId(1L)
                .name("Freelance")
                .type("income")   // minúscula
                .build();

        when(categoryGateway.existsByUserIdAndNameAndType(1L, "FREELANCE", "INCOME")).thenReturn(false);
        when(categoryGateway.save(any(Category.class))).thenReturn(
                Category.builder().id(7L).userId(1L).name("FREELANCE").type("INCOME").build());

        // Act
        Category result = categoryUseCase.createCategory(incomeCategory);

        // Assert
        assertThat(result.getName()).isEqualTo("FREELANCE");
        assertThat(result.getType()).isEqualTo("INCOME");
    }

    // ─────────────────────────────────────────────
    // CP-05-EXT-05: Tipo inválido con casing mixto (no se debe normalizar a válido)
    // "iNcOmE" normalizado a "INCOME" → válido; pero "savings" → "SAVINGS" → inválido
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-EXT-05: Tipo 'Savings' (casing mixto) es rechazado – no pertenece al conjunto válido")
    void CP_05_EXT_05_createCategory_mixedCaseInvalidType_throwsInvalidIncomeException() {
        // Arrange
        Category categoryInvalidType = Category.builder()
                .userId(1L)
                .name("Ahorro")
                .type("Savings")   // tras toUpperCase → "SAVINGS" → no válido
                .build();

        // Act & Assert
        assertThatThrownBy(() -> categoryUseCase.createCategory(categoryInvalidType))
                .isInstanceOf(InvalidIncomeException.class)
                .hasMessage(DomainConstants.CATEGORY_TYPE_INVALID);

        verifyNoInteractions(categoryGateway);
    }

    // ─────────────────────────────────────────────
    // CP-05-EXT-06: Duplicado es rechazado sin importar el casing original del nombre
    // El nombre se normaliza antes de consultar el gateway
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-EXT-06: Duplicado detectado correctamente con nombre en minúscula en el input")
    void CP_05_EXT_06_createCategory_duplicateDetectedAfterNormalization() {
        // Arrange – input con nombre en minúscula; el gateway tiene "ALIMENTACIÓN" → duplicado
        Category duplicateInLower = Category.builder()
                .userId(1L)
                .name("alimentación")
                .type("EXPENSE")
                .build();

        when(categoryGateway.existsByUserIdAndNameAndType(1L, "ALIMENTACIÓN", "EXPENSE")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryUseCase.createCategory(duplicateInLower))
                .isInstanceOf(DuplicateCategoryException.class)
                .hasMessage(DomainConstants.DUPLICATE_CATEGORY);

        verify(categoryGateway, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // CP-05-EXT-07: getCategoriesByType sin resultados retorna lista vacía (EXPENSE)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-EXT-07: getCategoriesByType sin categorías EXPENSE retorna lista vacía")
    void CP_05_EXT_07_getCategoriesByType_noExpenseCategories_returnsEmptyList() {
        // Arrange
        when(categoryGateway.findByUserIdAndType(1L, "EXPENSE")).thenReturn(List.of());

        // Act
        List<Category> result = categoryUseCase.getCategoriesByType(1L, "EXPENSE");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(categoryGateway).findByUserIdAndType(1L, "EXPENSE");
    }

    // ─────────────────────────────────────────────
    // CP-05-EXT-08: getAllCategories retorna mezcla de tipos INCOME y EXPENSE
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-EXT-08: getAllCategories retorna categorías de ambos tipos sin filtrar")
    void CP_05_EXT_08_getAllCategories_mixedTypes_returnsAll() {
        // Arrange
        List<Category> mixed = List.of(
                Category.builder().id(1L).userId(1L).name("SALARIO").type("INCOME").build(),
                Category.builder().id(2L).userId(1L).name("MERCADO").type("EXPENSE").build(),
                Category.builder().id(3L).userId(1L).name("FREELANCE").type("INCOME").build()
        );
        when(categoryGateway.findByUserId(1L)).thenReturn(mixed);

        // Act
        List<Category> result = categoryUseCase.getAllCategories(1L);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.stream().map(Category::getType))
                .containsExactlyInAnyOrder("INCOME", "EXPENSE", "INCOME");
        verify(categoryGateway).findByUserId(1L);
    }
}