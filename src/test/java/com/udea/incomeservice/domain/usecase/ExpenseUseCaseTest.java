package com.udea.incomeservice.domain.usecase;

import com.udea.incomeservice.domain.exception.DomainConstants;
import com.udea.incomeservice.domain.exception.InvalidExpenseException;
import com.udea.incomeservice.domain.gateway.CategoryGateway;
import com.udea.incomeservice.domain.gateway.ExpenseGateway;
import com.udea.incomeservice.domain.model.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CP-04: Casos de prueba - ExpenseUseCase")
class ExpenseUseCaseTest {

    @Mock
    private ExpenseGateway expenseGateway;

    @Mock
    private CategoryGateway categoryGateway;

    @InjectMocks
    private ExpenseUseCase expenseUseCase;

    private Expense validExpense;

    @BeforeEach
    void setUp() {
        validExpense = Expense.builder()
                .userId(1L)
                .amount(new BigDecimal("200.00"))
                .description("Supermercado")
                .date(LocalDate.now())
                .categoryId(20L)
                .build();
    }

    // ─────────────────────────────────────────────
    // CP-04-01: Registro exitoso de gasto
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-01: Registro exitoso de gasto – 201 Created, balance recalculado")
    void CP_04_01_registerExpense_happyPath_persistsAndBalanceRecalculated() {
        // Arrange
        Expense saved = Expense.builder()
                .id(5L)
                .userId(validExpense.getUserId())
                .amount(validExpense.getAmount())
                .description(validExpense.getDescription())
                .date(validExpense.getDate())
                .categoryId(validExpense.getCategoryId())
                .categoryName("Alimentación")
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryGateway.existsById(validExpense.getCategoryId())).thenReturn(true);
        when(expenseGateway.save(any(Expense.class))).thenReturn(saved);

        // Act
        Expense result = expenseUseCase.registerExpense(validExpense);

        // Assert – el gasto se persistió correctamente
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(result.getDescription()).isEqualTo("Supermercado");
        assertThat(result.getDate()).isEqualTo(validExpense.getDate());
        assertThat(result.getCategoryId()).isEqualTo(20L);

        // Assert – se invocó save (que internamente actualiza datos necesarios para el balance)
        verify(expenseGateway).save(validExpense);
        verify(categoryGateway).existsById(validExpense.getCategoryId());
    }

    // ─────────────────────────────────────────────
    // CP-04-02: Registro con fecha futura
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-02: Fecha futura lanza InvalidExpenseException – 400 Bad Request")
    void CP_04_02_registerExpense_futureDate_throwsInvalidExpenseException() {
        // Arrange – fecha posterior a hoy
        Expense expenseWithFutureDate = Expense.builder()
                .userId(1L)
                .amount(new BigDecimal("150.00"))
                .description("Pago anticipado")
                .date(LocalDate.now().plusDays(1))
                .categoryId(20L)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> expenseUseCase.registerExpense(expenseWithFutureDate))
                .isInstanceOf(InvalidExpenseException.class)
                .hasMessage(DomainConstants.DATE_CANNOT_BE_FUTURE);

        verifyNoInteractions(expenseGateway);
    }

    // ─────────────────────────────────────────────
    // CP-04-03: Registro con monto inválido (<= 0)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-03: Monto cero lanza InvalidExpenseException – 400 Bad Request")
    void CP_04_03_registerExpense_zeroAmount_throwsInvalidExpenseException() {
        // Arrange
        Expense expenseWithZeroAmount = Expense.builder()
                .userId(1L)
                .amount(BigDecimal.ZERO)
                .description("Cargo erróneo")
                .date(LocalDate.now())
                .categoryId(20L)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> expenseUseCase.registerExpense(expenseWithZeroAmount))
                .isInstanceOf(InvalidExpenseException.class)
                .hasMessage(DomainConstants.AMOUNT_MUST_BE_POSITIVE);

        verifyNoInteractions(expenseGateway);
    }

    @Test
    @DisplayName("CP-04-03: Monto negativo lanza InvalidExpenseException – 400 Bad Request")
    void CP_04_03_registerExpense_negativeAmount_throwsInvalidExpenseException() {
        // Arrange
        Expense expenseWithNegativeAmount = Expense.builder()
                .userId(1L)
                .amount(new BigDecimal("-50.00"))
                .description("Reverso inválido")
                .date(LocalDate.now())
                .categoryId(20L)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> expenseUseCase.registerExpense(expenseWithNegativeAmount))
                .isInstanceOf(InvalidExpenseException.class)
                .hasMessage(DomainConstants.AMOUNT_MUST_BE_POSITIVE);

        verifyNoInteractions(expenseGateway);
    }
    // ─────────────────────────────────────────────
    // CP-04-04: Categoría inexistente bloquea registro de gasto
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-04: Categoría inexistente lanza InvalidExpenseException – 400 Bad Request")
    void CP_04_04_registerExpense_categoryNotFound_throwsInvalidExpenseException() {
        // Arrange
        when(categoryGateway.existsById(validExpense.getCategoryId())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> expenseUseCase.registerExpense(validExpense))
                .isInstanceOf(InvalidExpenseException.class)
                .hasMessage(DomainConstants.CATEGORY_NOT_FOUND);

        verify(categoryGateway).existsById(validExpense.getCategoryId());
        verify(expenseGateway, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // CP-04-05: Consulta de gastos mensuales
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-05: Consulta mensual retorna lista del gateway para año y mes dados")
    void CP_04_05_getMonthlyExpenses_delegatesToGateway_returnsResult() {
        // Arrange
        List<Expense> expected = List.of(
                Expense.builder().id(1L).userId(1L).amount(new BigDecimal("200.00"))
                        .date(LocalDate.of(2024, 5, 3)).categoryId(20L).build(),
                Expense.builder().id(2L).userId(1L).amount(new BigDecimal("80.00"))
                        .date(LocalDate.of(2024, 5, 10)).categoryId(20L).build()
        );
        when(expenseGateway.findByUserIdAndMonth(1L, 2024, 5)).thenReturn(expected);

        // Act
        List<Expense> result = expenseUseCase.getMonthlyExpenses(1L, 2024, 5);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expected);
        verify(expenseGateway).findByUserIdAndMonth(1L, 2024, 5);
    }

    @Test
    @DisplayName("CP-04-05: Consulta mensual sin gastos retorna lista vacía")
    void CP_04_05_getMonthlyExpenses_noResults_returnsEmptyList() {
        // Arrange
        when(expenseGateway.findByUserIdAndMonth(1L, 2024, 5)).thenReturn(List.of());

        // Act
        List<Expense> result = expenseUseCase.getMonthlyExpenses(1L, 2024, 5);

        // Assert
        assertThat(result).isEmpty();
        verify(expenseGateway).findByUserIdAndMonth(1L, 2024, 5);
    }

    // ─────────────────────────────────────────────
    // CP-04-06: Consulta de todos los gastos del usuario
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-06: getAllExpenses delega al gateway y retorna todos los registros del usuario")
    void CP_04_06_getAllExpenses_delegatesToGateway_returnsAllRecords() {
        // Arrange
        List<Expense> expected = List.of(
                Expense.builder().id(1L).userId(1L).amount(new BigDecimal("200.00"))
                        .date(LocalDate.of(2024, 4, 5)).categoryId(20L).build()
        );
        when(expenseGateway.findByUserId(1L)).thenReturn(expected);

        // Act
        List<Expense> result = expenseUseCase.getAllExpenses(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(expected);
        verify(expenseGateway).findByUserId(1L);
    }
}
