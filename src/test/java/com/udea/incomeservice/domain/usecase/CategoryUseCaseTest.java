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

@ExtendWith(MockitoExtension.class)
@DisplayName("CP-05: Casos de prueba - CategoryUseCase")
class CategoryUseCaseTest {

    @Mock
    private CategoryGateway categoryGateway;

    @InjectMocks
    private CategoryUseCase categoryUseCase;

    private Category validCategory;

    @BeforeEach
    void setUp() {
        validCategory = Category.builder()
                .userId(1L)
                .name("  salario  ")   // con espacios para verificar trim
                .type("income")        // en minúscula para verificar toUpperCase
                .build();
    }

    // ─────────────────────────────────────────────
    // CP-05-01: Creación exitosa de categoría
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-01: Creación exitosa – nombre en MAYÚSCULAS y sin espacios, 201 Created")
    void CP_05_01_createCategory_happyPath_nameUpperCasedAndTrimmed() {
        // Arrange
        Category saved = Category.builder()
                .id(1L)
                .userId(1L)
                .name("SALARIO")
                .type("INCOME")
                .build();

        when(categoryGateway.existsByUserIdAndNameAndType(1L, "SALARIO", "INCOME")).thenReturn(false);
        when(categoryGateway.save(any(Category.class))).thenReturn(saved);

        // Act
        Category result = categoryUseCase.createCategory(validCategory);

        // Assert – resultado persistido
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("SALARIO");
        assertThat(result.getType()).isEqualTo("INCOME");

        // Assert – lo que se pasó al gateway tiene el nombre trimmed y en mayúsculas
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryGateway).save(captor.capture());
        Category captured = captor.getValue();
        assertThat(captured.getName()).isEqualTo("SALARIO");
        assertThat(captured.getType()).isEqualTo("INCOME");
    }

    // ─────────────────────────────────────────────
    // CP-05-02: Nombre duplicado para el mismo usuario
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-02: Categoría duplicada lanza DuplicateCategoryException – 409 Conflict")
    void CP_05_02_createCategory_duplicateName_throwsDuplicateCategoryException() {
        // Arrange – la categoría ya existe para este usuario
        when(categoryGateway.existsByUserIdAndNameAndType(1L, "SALARIO", "INCOME")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryUseCase.createCategory(validCategory))
                .isInstanceOf(DuplicateCategoryException.class)
                .hasMessage(DomainConstants.DUPLICATE_CATEGORY);

        verify(categoryGateway, never()).save(any());
    }

    @Test
    @DisplayName("CP-05-02: Mismo nombre para diferente tipo no genera conflicto")
    void CP_05_02_createCategory_sameName_differentType_noConflict() {
        // Arrange – existe INCOME pero no EXPENSE con ese nombre
        Category expenseCategory = Category.builder()
                .userId(1L)
                .name("  salario  ")
                .type("EXPENSE")
                .build();

        when(categoryGateway.existsByUserIdAndNameAndType(1L, "SALARIO", "EXPENSE")).thenReturn(false);
        when(categoryGateway.save(any(Category.class))).thenReturn(
                Category.builder().id(2L).userId(1L).name("SALARIO").type("EXPENSE").build());

        // Act
        Category result = categoryUseCase.createCategory(expenseCategory);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("EXPENSE");
        verify(categoryGateway).save(any());
    }

    // ─────────────────────────────────────────────
    // CP-05-03: Tipo de categoría inválido
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-03: Tipo inválido lanza InvalidIncomeException – 400 Bad Request")
    void CP_05_03_createCategory_invalidType_throwsInvalidIncomeException() {
        // Arrange
        Category categoryWithInvalidType = Category.builder()
                .userId(1L)
                .name("Ahorro")
                .type("SAVINGS")   // tipo no permitido
                .build();

        // Act & Assert
        assertThatThrownBy(() -> categoryUseCase.createCategory(categoryWithInvalidType))
                .isInstanceOf(InvalidIncomeException.class)
                .hasMessage(DomainConstants.CATEGORY_TYPE_INVALID);

        verifyNoInteractions(categoryGateway);
    }

    @Test
    @DisplayName("CP-05-03: Tipo vacío lanza InvalidIncomeException – 400 Bad Request")
    void CP_05_03_createCategory_emptyType_throwsInvalidIncomeException() {
        // Arrange
        Category categoryWithEmptyType = Category.builder()
                .userId(1L)
                .name("Varios")
                .type("")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> categoryUseCase.createCategory(categoryWithEmptyType))
                .isInstanceOf(InvalidIncomeException.class)
                .hasMessage(DomainConstants.CATEGORY_TYPE_INVALID);

        verifyNoInteractions(categoryGateway);
    }
    // ─────────────────────────────────────────────
    // CP-05-04: Consulta de categorías por tipo
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-04: getCategoriesByType convierte tipo a mayúsculas y delega al gateway")
    void CP_05_04_getCategoriesByType_convertsTypeToUpperCase_delegatesToGateway() {
        // Arrange
        List<Category> expected = List.of(
                Category.builder().id(1L).userId(1L).name("SALARIO").type("INCOME").build(),
                Category.builder().id(2L).userId(1L).name("ARRIENDO").type("INCOME").build()
        );
        when(categoryGateway.findByUserIdAndType(1L, "INCOME")).thenReturn(expected);

        // Act – se pasa en minúscula para validar la conversión
        List<Category> result = categoryUseCase.getCategoriesByType(1L, "income");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expected);
        verify(categoryGateway).findByUserIdAndType(1L, "INCOME");
    }

    @Test
    @DisplayName("CP-05-04: getCategoriesByType sin coincidencias retorna lista vacía")
    void CP_05_04_getCategoriesByType_noResults_returnsEmptyList() {
        // Arrange
        when(categoryGateway.findByUserIdAndType(1L, "EXPENSE")).thenReturn(List.of());

        // Act
        List<Category> result = categoryUseCase.getCategoriesByType(1L, "expense");

        // Assert
        assertThat(result).isEmpty();
        verify(categoryGateway).findByUserIdAndType(1L, "EXPENSE");
    }

    // ─────────────────────────────────────────────
    // CP-05-05: Consulta de todas las categorías del usuario
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-05-05: getAllCategories delega al gateway y retorna todas las categorías del usuario")
    void CP_05_05_getAllCategories_delegatesToGateway_returnsAllCategories() {
        // Arrange
        List<Category> expected = List.of(
                Category.builder().id(1L).userId(1L).name("SALARIO").type("INCOME").build(),
                Category.builder().id(2L).userId(1L).name("MERCADO").type("EXPENSE").build()
        );
        when(categoryGateway.findByUserId(1L)).thenReturn(expected);

        // Act
        List<Category> result = categoryUseCase.getAllCategories(1L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expected);
        verify(categoryGateway).findByUserId(1L);
    }
}
