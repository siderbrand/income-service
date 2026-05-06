package com.udea.incomeservice.domain.usecase;

import com.udea.incomeservice.domain.exception.DomainConstants;
import com.udea.incomeservice.domain.exception.DuplicateBudgetException;
import com.udea.incomeservice.domain.exception.InvalidExpenseException;
import com.udea.incomeservice.domain.gateway.BudgetGateway;
import com.udea.incomeservice.domain.gateway.CategoryGateway;
import com.udea.incomeservice.domain.gateway.ExpenseGateway;
import com.udea.incomeservice.domain.model.Budget;
import com.udea.incomeservice.domain.model.BudgetStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CP-06: Casos de prueba - BudgetUseCase")
class BudgetUseCaseTest {

    @Mock
    private BudgetGateway budgetGateway;

    @Mock
    private ExpenseGateway expenseGateway;

    @Mock
    private CategoryGateway categoryGateway;

    @InjectMocks
    private BudgetUseCase budgetUseCase;

    private Budget validBudget;

    @BeforeEach
    void setUp() {
        validBudget = Budget.builder()
                .userId(1L)
                .categoryId(10L)
                .categoryName("Alimentación")
                .maxAmount(new BigDecimal("500.00"))
                .build();
    }

    // ─────────────────────────────────────────────
    // CP-06-01: Creación exitosa de presupuesto
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-01: Creación exitosa de presupuesto – 201 Created, sin duplicado previo")
    void CP_06_01_createBudget_happyPath_persistsBudget() {
        // Arrange
        Budget saved = Budget.builder()
                .id(1L)
                .userId(validBudget.getUserId())
                .categoryId(validBudget.getCategoryId())
                .categoryName(validBudget.getCategoryName())
                .maxAmount(validBudget.getMaxAmount())
                .build();

        when(categoryGateway.existsById(validBudget.getCategoryId())).thenReturn(true);
        when(budgetGateway.existsByUserIdAndCategoryId(validBudget.getUserId(), validBudget.getCategoryId()))
                .thenReturn(false);
        when(budgetGateway.save(any(Budget.class))).thenReturn(saved);

        // Act
        Budget result = budgetUseCase.createBudget(validBudget);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMaxAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getCategoryId()).isEqualTo(10L);

        verify(budgetGateway).existsByUserIdAndCategoryId(1L, 10L);
        verify(budgetGateway).save(validBudget);
    }

    // ─────────────────────────────────────────────
    // CP-06-02: Alerta de presupuesto al alcanzar el 80%
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-02: Gasto al 80% del presupuesto – BudgetStatus WARNING")
    void CP_06_02_checkBudgetAlert_spentAtWarningThreshold_returnsWarningStatus() {
        // Arrange – presupuesto 500, gasto 400 = 80% exacto → WARNING
        Budget budget = Budget.builder()
                .id(1L)
                .userId(1L)
                .categoryId(10L)
                .categoryName("Alimentación")
                .maxAmount(new BigDecimal("500.00"))
                .build();

        when(budgetGateway.findByUserIdAndCategoryId(1L, 10L)).thenReturn(Optional.of(budget));
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("400.00"));

        // Act
        BudgetStatus result = budgetUseCase.checkBudgetAlert(1L, 10L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.WARNING);
        assertThat(result.getPercentage()).isEqualTo(80.0);
        assertThat(result.getSpentAmount()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(result.getAvailableAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("CP-06-02: Gasto por encima del 80% pero por debajo del 100% – BudgetStatus WARNING")
    void CP_06_02_checkBudgetAlert_spentAboveWarningBelowExceeded_returnsWarningStatus() {
        // Arrange – presupuesto 500, gasto 450 = 90% → WARNING
        Budget budget = Budget.builder()
                .id(1L)
                .userId(1L)
                .categoryId(10L)
                .categoryName("Alimentación")
                .maxAmount(new BigDecimal("500.00"))
                .build();

        when(budgetGateway.findByUserIdAndCategoryId(1L, 10L)).thenReturn(Optional.of(budget));
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("450.00"));

        // Act
        BudgetStatus result = budgetUseCase.checkBudgetAlert(1L, 10L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.WARNING);
        assertThat(result.getPercentage()).isEqualTo(90.0);
    }

    // ─────────────────────────────────────────────
    // CP-06-03: Presupuesto agotado (gastos >= presupuesto)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-03: Gasto igual al presupuesto – BudgetStatus EXCEEDED")
    void CP_06_03_checkBudgetAlert_spentEqualsMax_returnsExceededStatus() {
        // Arrange – presupuesto 500, gasto 500 = 100% → EXCEEDED
        Budget budget = Budget.builder()
                .id(1L)
                .userId(1L)
                .categoryId(10L)
                .categoryName("Alimentación")
                .maxAmount(new BigDecimal("500.00"))
                .build();

        when(budgetGateway.findByUserIdAndCategoryId(1L, 10L)).thenReturn(Optional.of(budget));
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("500.00"));

        // Act
        BudgetStatus result = budgetUseCase.checkBudgetAlert(1L, 10L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.EXCEEDED);
        assertThat(result.getPercentage()).isEqualTo(100.0);
        assertThat(result.getAvailableAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("CP-06-03: Gasto mayor al presupuesto – BudgetStatus EXCEEDED")
    void CP_06_03_checkBudgetAlert_spentExceedsMax_returnsExceededStatus() {
        // Arrange – presupuesto 500, gasto 600 = 120% → EXCEEDED
        Budget budget = Budget.builder()
                .id(1L)
                .userId(1L)
                .categoryId(10L)
                .categoryName("Alimentación")
                .maxAmount(new BigDecimal("500.00"))
                .build();

        when(budgetGateway.findByUserIdAndCategoryId(1L, 10L)).thenReturn(Optional.of(budget));
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("600.00"));

        // Act
        BudgetStatus result = budgetUseCase.checkBudgetAlert(1L, 10L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.EXCEEDED);
        assertThat(result.getPercentage()).isEqualTo(120.0);
        assertThat(result.getAvailableAmount()).isEqualByComparingTo(new BigDecimal("-100.00"));
    }

    // ─────────────────────────────────────────────
    // CP-06-04: Presupuesto duplicado para la misma categoría
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-04: Presupuesto duplicado lanza DuplicateBudgetException – 409 Conflict")
    void CP_06_04_createBudget_duplicate_throwsDuplicateBudgetException() {
        // Arrange
        when(categoryGateway.existsById(validBudget.getCategoryId())).thenReturn(true);
        when(budgetGateway.existsByUserIdAndCategoryId(validBudget.getUserId(), validBudget.getCategoryId()))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> budgetUseCase.createBudget(validBudget))
                .isInstanceOf(DuplicateBudgetException.class)
                .hasMessage(DomainConstants.BUDGET_ALREADY_EXISTS);

        verify(budgetGateway, never()).save(any());
    }
    // ─────────────────────────────────────────────
    // CP-06-05: Creación con monto inválido (<= 0)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-05: Monto cero lanza InvalidExpenseException – 400 Bad Request")
    void CP_06_05_createBudget_zeroAmount_throwsInvalidExpenseException() {
        // Arrange
        Budget budgetWithZeroAmount = Budget.builder()
                .userId(1L)
                .categoryId(10L)
                .categoryName("Alimentación")
                .maxAmount(BigDecimal.ZERO)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> budgetUseCase.createBudget(budgetWithZeroAmount))
                .isInstanceOf(InvalidExpenseException.class)
                .hasMessage(DomainConstants.AMOUNT_MUST_BE_POSITIVE);

        verifyNoInteractions(budgetGateway, categoryGateway);
    }

    @Test
    @DisplayName("CP-06-05: Monto negativo lanza InvalidExpenseException – 400 Bad Request")
    void CP_06_05_createBudget_negativeAmount_throwsInvalidExpenseException() {
        // Arrange
        Budget budgetWithNegativeAmount = Budget.builder()
                .userId(1L)
                .categoryId(10L)
                .categoryName("Alimentación")
                .maxAmount(new BigDecimal("-100.00"))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> budgetUseCase.createBudget(budgetWithNegativeAmount))
                .isInstanceOf(InvalidExpenseException.class)
                .hasMessage(DomainConstants.AMOUNT_MUST_BE_POSITIVE);

        verifyNoInteractions(budgetGateway, categoryGateway);
    }

    // ─────────────────────────────────────────────
    // CP-06-06: Creación con categoría inexistente
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-06: Categoría inexistente lanza InvalidExpenseException – 400 Bad Request")
    void CP_06_06_createBudget_categoryNotFound_throwsInvalidExpenseException() {
        // Arrange
        when(categoryGateway.existsById(validBudget.getCategoryId())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> budgetUseCase.createBudget(validBudget))
                .isInstanceOf(InvalidExpenseException.class)
                .hasMessage(DomainConstants.CATEGORY_NOT_FOUND);

        verify(categoryGateway).existsById(validBudget.getCategoryId());
        verify(budgetGateway, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // CP-06-07: getBudgetStatuses retorna lista completa con estados calculados
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-07: getBudgetStatuses retorna un BudgetStatus por cada presupuesto del usuario")
    void CP_06_07_getBudgetStatuses_multipleBudgets_returnsStatusForEach() {
        // Arrange
        Budget budget1 = Budget.builder().id(1L).userId(1L).categoryId(10L)
                .categoryName("Alimentación").maxAmount(new BigDecimal("500.00")).build();
        Budget budget2 = Budget.builder().id(2L).userId(1L).categoryId(11L)
                .categoryName("Transporte").maxAmount(new BigDecimal("200.00")).build();

        when(budgetGateway.findByUserId(1L)).thenReturn(List.of(budget1, budget2));
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("100.00")); // 20% → OK
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(11L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("200.00")); // 100% → EXCEEDED

        // Act
        List<BudgetStatus> result = budgetUseCase.getBudgetStatuses(1L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(BudgetStatus.OK);
        assertThat(result.get(1).getStatus()).isEqualTo(BudgetStatus.EXCEEDED);
        verify(budgetGateway).findByUserId(1L);
    }

    @Test
    @DisplayName("CP-06-07: getBudgetStatuses sin presupuestos retorna lista vacía")
    void CP_06_07_getBudgetStatuses_noBudgets_returnsEmptyList() {
        // Arrange
        when(budgetGateway.findByUserId(1L)).thenReturn(List.of());

        // Act
        List<BudgetStatus> result = budgetUseCase.getBudgetStatuses(1L);

        // Assert
        assertThat(result).isEmpty();
        verify(budgetGateway).findByUserId(1L);
        verifyNoInteractions(expenseGateway);
    }

    // ─────────────────────────────────────────────
    // CP-06-08: checkBudgetAlert sin presupuesto configurado
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-08: checkBudgetAlert sin presupuesto para esa categoría retorna null")
    void CP_06_08_checkBudgetAlert_noBudgetForCategory_returnsNull() {
        // Arrange
        when(budgetGateway.findByUserIdAndCategoryId(1L, 99L)).thenReturn(Optional.empty());

        // Act
        BudgetStatus result = budgetUseCase.checkBudgetAlert(1L, 99L);

        // Assert
        assertThat(result).isNull();
        verify(budgetGateway).findByUserIdAndCategoryId(1L, 99L);
        verifyNoInteractions(expenseGateway);
    }

    // ─────────────────────────────────────────────
    // CP-06-09: Presupuesto con bajo porcentaje de uso → OK
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-09: Gasto por debajo del 80% del presupuesto – BudgetStatus OK")
    void CP_06_09_checkBudgetAlert_spentBelowWarningThreshold_returnsOkStatus() {
        // Arrange – presupuesto 500, gasto 300 = 60% → OK
        Budget budget = Budget.builder()
                .id(1L).userId(1L).categoryId(10L)
                .categoryName("Alimentación")
                .maxAmount(new BigDecimal("500.00"))
                .build();

        when(budgetGateway.findByUserIdAndCategoryId(1L, 10L)).thenReturn(Optional.of(budget));
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("300.00"));

        // Act
        BudgetStatus result = budgetUseCase.checkBudgetAlert(1L, 10L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.OK);
        assertThat(result.getPercentage()).isEqualTo(60.0);
        assertThat(result.getAvailableAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
    }
}