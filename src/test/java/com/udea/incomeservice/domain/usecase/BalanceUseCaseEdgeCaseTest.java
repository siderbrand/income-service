package com.udea.incomeservice.domain.usecase;

import com.udea.incomeservice.domain.gateway.ExpenseGateway;
import com.udea.incomeservice.domain.gateway.IncomeGateway;
import com.udea.incomeservice.domain.model.MonthlyBalance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * CP-07-EXT: Casos de prueba complementarios para BalanceUseCase.
 *
 * Cubren: valores exactos de la HU-07, balance con ingresos cero,
 * actualización automática tras nueva transacción (simulada mediante
 * dos llamadas consecutivas), y aislamiento por userId.
 *
 * HU relacionada: HU-07 – Balance Financiero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CP-07-EXT: Casos límite y cobertura adicional – BalanceUseCase")
class BalanceUseCaseEdgeCaseTest {

    @Mock
    private IncomeGateway incomeGateway;

    @Mock
    private ExpenseGateway expenseGateway;

    @InjectMocks
    private BalanceUseCase balanceUseCase;

    // ─────────────────────────────────────────────
    // CP-07-EXT-01: Valores exactos del dashboard de la HU-07
    // Ingresos $3.000.000 – Gastos $1.800.000 = Balance $1.200.000 (POSITIVE)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-07-EXT-01: Balance positivo con valores exactos de HU-07 ($3.000.000 - $1.800.000 = $1.200.000)")
    void CP_07_EXT_01_getMonthlyBalance_huExactValues_positiveBalanceWithCorrectTotals() {
        // Arrange – escenario exacto descrito en los criterios de aceptación de HU-07
        when(incomeGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("3000000.00"));
        when(expenseGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("1800000.00"));

        // Act
        MonthlyBalance result = balanceUseCase.getMonthlyBalance(1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(MonthlyBalance.POSITIVE);
        assertThat(result.getTotalIncomes()).isEqualByComparingTo(new BigDecimal("3000000.00"));
        assertThat(result.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("1800000.00"));
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("1200000.00"));
    }

    // ─────────────────────────────────────────────
    // CP-07-EXT-02: Valores exactos del escenario negativo de la HU-07
    // Gastos $2.500.000 > Ingresos $2.000.000 → Balance -$500.000 (NEGATIVE)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-07-EXT-02: Balance negativo con valores exactos de HU-07 ($2.000.000 - $2.500.000 = -$500.000)")
    void CP_07_EXT_02_getMonthlyBalance_huNegativeExactValues_negativeBalanceWithAlert() {
        // Arrange – escenario de balance negativo de HU-07
        when(incomeGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("2000000.00"));
        when(expenseGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("2500000.00"));

        // Act
        MonthlyBalance result = balanceUseCase.getMonthlyBalance(1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(MonthlyBalance.NEGATIVE);
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("-500000.00"));
        assertThat(result.getTotalExpenses()).isGreaterThan(result.getTotalIncomes());
    }

    // ─────────────────────────────────────────────
    // CP-07-EXT-03: Actualización automática del balance tras nuevo gasto
    // Criterio HU-07: "el balance se actualiza automáticamente a $650.000"
    // Se simula con dos llamadas consecutivas al use case
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-07-EXT-03: Balance actualizado tras registrar nuevo gasto ($800.000 - $150.000 = $650.000)")
    void CP_07_EXT_03_getMonthlyBalance_afterNewExpense_balanceDecreasedCorrectly() {
        // Arrange – balance inicial: ingresos 1.000.000, gastos 200.000 → 800.000
        // Luego se registra un nuevo gasto de 150.000 → gastos totales 350.000 → balance 650.000
        when(incomeGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("1000000.00"));

        // Primera consulta: gastos = 200.000
        when(expenseGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("200000.00"))
                // Segunda consulta (tras registrar el nuevo gasto): gastos = 350.000
                .thenReturn(new BigDecimal("350000.00"));

        // Act – primera consulta del dashboard
        MonthlyBalance balanceBefore = balanceUseCase.getMonthlyBalance(1L);

        // Act – segunda consulta tras el nuevo gasto
        MonthlyBalance balanceAfter = balanceUseCase.getMonthlyBalance(1L);

        // Assert – antes del gasto
        assertThat(balanceBefore.getStatus()).isEqualTo(MonthlyBalance.POSITIVE);
        assertThat(balanceBefore.getBalance()).isEqualByComparingTo(new BigDecimal("800000.00"));

        // Assert – después del gasto ($150.000 adicionales)
        assertThat(balanceAfter.getStatus()).isEqualTo(MonthlyBalance.POSITIVE);
        assertThat(balanceAfter.getBalance()).isEqualByComparingTo(new BigDecimal("650000.00"));

        // El balance disminuyó exactamente en el monto del nuevo gasto
        BigDecimal decrease = balanceBefore.getBalance().subtract(balanceAfter.getBalance());
        assertThat(decrease).isEqualByComparingTo(new BigDecimal("150000.00"));
    }

    // ─────────────────────────────────────────────
    // CP-07-EXT-04: Balance con ingresos = 0 → balance negativo = -gastos
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-07-EXT-04: Sin ingresos y con gastos – balance negativo igual al total de gastos")
    void CP_07_EXT_04_getMonthlyBalance_noIncomes_balanceEqualsNegativeExpenses() {
        // Arrange
        when(incomeGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(BigDecimal.ZERO);
        when(expenseGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("500000.00"));

        // Act
        MonthlyBalance result = balanceUseCase.getMonthlyBalance(1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(MonthlyBalance.NEGATIVE);
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("-500000.00"));
        assertThat(result.getTotalIncomes()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ─────────────────────────────────────────────
    // CP-07-EXT-05: Aislamiento por userId – cada usuario tiene su propio balance
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-07-EXT-05: Dos usuarios con distintos balances son calculados de forma independiente")
    void CP_07_EXT_05_getMonthlyBalance_differentUsers_independentBalances() {
        // Arrange – usuario 1: positivo; usuario 2: negativo
        when(incomeGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("2000.00"));
        when(expenseGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("500.00"));

        when(incomeGateway.sumByUserIdAndMonth(eq(2L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("500.00"));
        when(expenseGateway.sumByUserIdAndMonth(eq(2L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("800.00"));

        // Act
        MonthlyBalance balanceUser1 = balanceUseCase.getMonthlyBalance(1L);
        MonthlyBalance balanceUser2 = balanceUseCase.getMonthlyBalance(2L);

        // Assert
        assertThat(balanceUser1.getStatus()).isEqualTo(MonthlyBalance.POSITIVE);
        assertThat(balanceUser1.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));

        assertThat(balanceUser2.getStatus()).isEqualTo(MonthlyBalance.NEGATIVE);
        assertThat(balanceUser2.getBalance()).isEqualByComparingTo(new BigDecimal("-300.00"));

        // Verifica que los gateways fueron llamados con los userId correctos
        verify(incomeGateway).sumByUserIdAndMonth(eq(1L), anyInt(), anyInt());
        verify(incomeGateway).sumByUserIdAndMonth(eq(2L), anyInt(), anyInt());
    }

    // ─────────────────────────────────────────────
    // CP-07-EXT-06: Balance con ingresos muy pequeños y gastos decimales
    // Verifica precisión de BigDecimal en el cálculo
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-07-EXT-06: Cálculo con valores decimales – precisión de BigDecimal garantizada")
    void CP_07_EXT_06_getMonthlyBalance_decimalValues_precisionMaintained() {
        // Arrange
        when(incomeGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("1000.50"));
        when(expenseGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("999.75"));

        // Act
        MonthlyBalance result = balanceUseCase.getMonthlyBalance(1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(MonthlyBalance.POSITIVE);
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("0.75"));
    }
}