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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CP-07: Casos de prueba - BalanceUseCase")
class BalanceUseCaseTest {

    @Mock
    private IncomeGateway incomeGateway;

    @Mock
    private ExpenseGateway expenseGateway;

    @InjectMocks
    private BalanceUseCase balanceUseCase;

    // ─────────────────────────────────────────────
    // CP-07-01: Balance positivo (ingresos > gastos)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-07-01: Ingresos > Gastos – MonthlyBalance status POSITIVE")
    void CP_07_01_getMonthlyBalance_incomesExceedExpenses_returnsPositiveStatus() {
        // Arrange
        when(incomeGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("3000.00"));
        when(expenseGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("1800.00"));

        // Act
        MonthlyBalance result = balanceUseCase.getMonthlyBalance(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(MonthlyBalance.POSITIVE);
        assertThat(result.getTotalIncomes()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(result.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("1800.00"));
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("1200.00"));
    }

    // ─────────────────────────────────────────────
    // CP-07-02: Balance negativo (gastos > ingresos)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-07-02: Gastos > Ingresos – MonthlyBalance status NEGATIVE")
    void CP_07_02_getMonthlyBalance_expensesExceedIncomes_returnsNegativeStatus() {
        // Arrange
        when(incomeGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("1000.00"));
        when(expenseGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("2500.00"));

        // Act
        MonthlyBalance result = balanceUseCase.getMonthlyBalance(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(MonthlyBalance.NEGATIVE);
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("-1500.00"));
    }

    // ─────────────────────────────────────────────
    // CP-07-03: Balance neutro (ingresos == gastos)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-07-03: Ingresos == Gastos – MonthlyBalance status ZERO")
    void CP_07_03_getMonthlyBalance_incomesEqualExpenses_returnsZeroStatus() {
        // Arrange
        when(incomeGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("2000.00"));
        when(expenseGateway.sumByUserIdAndMonth(eq(1L), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("2000.00"));

        // Act
        MonthlyBalance result = balanceUseCase.getMonthlyBalance(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(MonthlyBalance.ZERO);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalIncomes()).isEqualByComparingTo(result.getTotalExpenses());
    }
    // ─────────────────────────────────────────────
    // CP-07-04: Balance consultado con año y mes específicos
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-07-04: getMonthlyBalance con año y mes específicos usa esos parámetros exactos")
    void CP_07_04_getMonthlyBalance_withSpecificYearAndMonth_usesExactParameters() {
        // Arrange
        when(incomeGateway.sumByUserIdAndMonth(1L, 2024, 3))
                .thenReturn(new BigDecimal("4000.00"));
        when(expenseGateway.sumByUserIdAndMonth(1L, 2024, 3))
                .thenReturn(new BigDecimal("2500.00"));

        // Act
        MonthlyBalance result = balanceUseCase.getMonthlyBalance(1L, 2024, 3);

        // Assert
        assertThat(result.getStatus()).isEqualTo(MonthlyBalance.POSITIVE);
        assertThat(result.getTotalIncomes()).isEqualByComparingTo(new BigDecimal("4000.00"));
        assertThat(result.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));

        verify(incomeGateway).sumByUserIdAndMonth(1L, 2024, 3);
        verify(expenseGateway).sumByUserIdAndMonth(1L, 2024, 3);
    }

    @Test
    @DisplayName("CP-07-04: getMonthlyBalance con gastos cero retorna balance igual a ingresos")
    void CP_07_04_getMonthlyBalance_noExpenses_balanceEqualsIncomes() {
        // Arrange
        when(incomeGateway.sumByUserIdAndMonth(1L, 2024, 6))
                .thenReturn(new BigDecimal("1500.00"));
        when(expenseGateway.sumByUserIdAndMonth(1L, 2024, 6))
                .thenReturn(BigDecimal.ZERO);

        // Act
        MonthlyBalance result = balanceUseCase.getMonthlyBalance(1L, 2024, 6);

        // Assert
        assertThat(result.getStatus()).isEqualTo(MonthlyBalance.POSITIVE);
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(result.getTotalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("CP-07-04: getMonthlyBalance con ingresos y gastos cero retorna balance ZERO")
    void CP_07_04_getMonthlyBalance_noIncomesNoExpenses_returnsZeroStatus() {
        // Arrange
        when(incomeGateway.sumByUserIdAndMonth(1L, 2024, 7))
                .thenReturn(BigDecimal.ZERO);
        when(expenseGateway.sumByUserIdAndMonth(1L, 2024, 7))
                .thenReturn(BigDecimal.ZERO);

        // Act
        MonthlyBalance result = balanceUseCase.getMonthlyBalance(1L, 2024, 7);

        // Assert
        assertThat(result.getStatus()).isEqualTo(MonthlyBalance.ZERO);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}