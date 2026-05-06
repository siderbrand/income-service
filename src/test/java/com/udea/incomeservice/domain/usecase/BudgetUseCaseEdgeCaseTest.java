package com.udea.incomeservice.domain.usecase;

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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CP-06-EXT: Casos de prueba complementarios para BudgetUseCase.
 *
 * Cubren: valores límite exactos del umbral 80%, gasto cero,
 * getBudgetStatuses con estados mixtos y cálculos de porcentaje
 * en condiciones de borde.
 *
 * HU relacionada: HU-06 – Presupuestos Mensuales
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CP-06-EXT: Casos límite y cobertura adicional – BudgetUseCase")
class BudgetUseCaseEdgeCaseTest {

    @Mock
    private BudgetGateway budgetGateway;

    @Mock
    private ExpenseGateway expenseGateway;

    @Mock
    private CategoryGateway categoryGateway;

    @InjectMocks
    private BudgetUseCase budgetUseCase;

    private Budget defaultBudget;

    @BeforeEach
    void setUp() {
        defaultBudget = Budget.builder()
                .id(1L)
                .userId(1L)
                .categoryId(10L)
                .categoryName("Salud")
                .maxAmount(new BigDecimal("300.00"))
                .build();
    }

    // ─────────────────────────────────────────────
    // CP-06-EXT-01: Exactamente 79.99% → estado OK (justo debajo del umbral WARNING)
    // Criterio HU-06: La alerta se activa al alcanzar el 80%
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-EXT-01: Gasto en 79.99% – justo bajo el umbral de WARNING, estado debe ser OK")
    void CP_06_EXT_01_checkBudgetAlert_justBelowWarningThreshold_returnsOkStatus() {
        // Arrange – presupuesto 300, gasto 239.97 ≈ 79.99%
        when(budgetGateway.findByUserIdAndCategoryId(1L, 10L)).thenReturn(Optional.of(defaultBudget));
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("239.97"));

        // Act
        BudgetStatus result = budgetUseCase.checkBudgetAlert(1L, 10L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.OK);
        assertThat(result.getPercentage()).isLessThan(80.0);
    }

    // ─────────────────────────────────────────────
    // CP-06-EXT-02: Exactamente 80% → estado WARNING
    // Criterio HU-06: "Estás cerca del límite" se activa al 80%
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-EXT-02: Gasto exactamente al 80% del presupuesto Salud ($300) – alerta WARNING activa")
    void CP_06_EXT_02_checkBudgetAlert_exactly80Percent_triggersWarning() {
        // Arrange – 300 * 80% = 240 → WARNING exacto (HU-06: presupuesto Salud $300.000, gasto $240.000)
        when(budgetGateway.findByUserIdAndCategoryId(1L, 10L)).thenReturn(Optional.of(defaultBudget));
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("240.00"));

        // Act
        BudgetStatus result = budgetUseCase.checkBudgetAlert(1L, 10L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.WARNING);
        assertThat(result.getPercentage()).isEqualTo(80.0);
        assertThat(result.getAvailableAmount()).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    // ─────────────────────────────────────────────
    // CP-06-EXT-03: Gasto = 0 → estado OK, disponible = maxAmount completo
    // Panel de control muestra progreso en 0% (HU-06: "el indicador inicia en 0%")
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-EXT-03: Gasto cero – indicador de progreso en 0%, disponible = maxAmount")
    void CP_06_EXT_03_checkBudgetAlert_zeroSpent_returnsOkWithFullBudgetAvailable() {
        // Arrange
        when(budgetGateway.findByUserIdAndCategoryId(1L, 10L)).thenReturn(Optional.of(defaultBudget));
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(BigDecimal.ZERO);

        // Act
        BudgetStatus result = budgetUseCase.checkBudgetAlert(1L, 10L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.OK);
        assertThat(result.getPercentage()).isEqualTo(0.0);
        assertThat(result.getSpentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getAvailableAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    // ─────────────────────────────────────────────
    // CP-06-EXT-04: Gasto = 99.99% → sigue siendo WARNING (no EXCEEDED)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-EXT-04: Gasto al 99.99% (justo antes de 100%) – estado WARNING, no EXCEEDED")
    void CP_06_EXT_04_checkBudgetAlert_justBelow100Percent_returnsWarningNotExceeded() {
        // Arrange – 300 * 99.99% ≈ 299.97
        when(budgetGateway.findByUserIdAndCategoryId(1L, 10L)).thenReturn(Optional.of(defaultBudget));
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("299.97"));

        // Act
        BudgetStatus result = budgetUseCase.checkBudgetAlert(1L, 10L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(BudgetStatus.WARNING);
        assertThat(result.getPercentage()).isLessThan(100.0);
        assertThat(result.getPercentage()).isGreaterThanOrEqualTo(80.0);
    }

    // ─────────────────────────────────────────────
    // CP-06-EXT-05: getBudgetStatuses con presupuestos en los tres estados posibles
    // Cubre el camino que itera sobre múltiples presupuestos con estados distintos
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-EXT-05: getBudgetStatuses con OK, WARNING y EXCEEDED en el mismo panel")
    void CP_06_EXT_05_getBudgetStatuses_allThreeStatuses_computedCorrectly() {
        // Arrange
        Budget budgetOk = Budget.builder().id(1L).userId(1L).categoryId(10L)
                .categoryName("Alimentación").maxAmount(new BigDecimal("500.00")).build();
        Budget budgetWarning = Budget.builder().id(2L).userId(1L).categoryId(11L)
                .categoryName("Salud").maxAmount(new BigDecimal("300.00")).build();
        Budget budgetExceeded = Budget.builder().id(3L).userId(1L).categoryId(12L)
                .categoryName("Transporte").maxAmount(new BigDecimal("200.00")).build();

        when(budgetGateway.findByUserId(1L)).thenReturn(List.of(budgetOk, budgetWarning, budgetExceeded));
        // OK: 50% de 500
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("250.00"));
        // WARNING: 90% de 300
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(11L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("270.00"));
        // EXCEEDED: 110% de 200
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(12L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("220.00"));

        // Act
        List<BudgetStatus> result = budgetUseCase.getBudgetStatuses(1L);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getStatus()).isEqualTo(BudgetStatus.OK);
        assertThat(result.get(1).getStatus()).isEqualTo(BudgetStatus.WARNING);
        assertThat(result.get(2).getStatus()).isEqualTo(BudgetStatus.EXCEEDED);

        // Verifica monto negativo disponible cuando se supera el presupuesto
        assertThat(result.get(2).getAvailableAmount()).isEqualByComparingTo(new BigDecimal("-20.00"));
    }

    // ─────────────────────────────────────────────
    // CP-06-EXT-06: getBudgetStatuses verifica campos del BudgetStatus resultante
    // Garantiza que maxAmount, categoryName y budgetId se mapean correctamente
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-06-EXT-06: getBudgetStatuses mapea correctamente todos los campos del BudgetStatus")
    void CP_06_EXT_06_getBudgetStatuses_singleBudget_allFieldsMappedCorrectly() {
        // Arrange
        Budget budget = Budget.builder()
                .id(99L)
                .userId(1L)
                .categoryId(10L)
                .categoryName("Entretenimiento")
                .maxAmount(new BigDecimal("100.00"))
                .build();

        when(budgetGateway.findByUserId(1L)).thenReturn(List.of(budget));
        when(expenseGateway.sumByUserIdAndCategoryIdAndMonth(eq(1L), eq(10L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("30.00")); // 30% → OK

        // Act
        List<BudgetStatus> result = budgetUseCase.getBudgetStatuses(1L);

        // Assert
        assertThat(result).hasSize(1);
        BudgetStatus status = result.get(0);
        assertThat(status.getBudgetId()).isEqualTo(99L);
        assertThat(status.getCategoryName()).isEqualTo("Entretenimiento");
        assertThat(status.getMaxAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(status.getSpentAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(status.getAvailableAmount()).isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(status.getPercentage()).isEqualTo(30.0);
        assertThat(status.getStatus()).isEqualTo(BudgetStatus.OK);
    }
}