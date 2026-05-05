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

/**
 * CP-04-EXT: Casos de prueba complementarios para ExpenseUseCase.
 *
 * Cubren valores límite de fecha y monto, aislamiento de usuarios,
 * y el orden de ejecución de las validaciones del dominio.
 *
 * HU relacionada: HU-04 – Registro de Gastos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CP-04-EXT: Casos límite y cobertura adicional – ExpenseUseCase")
class ExpenseUseCaseEdgeCaseTest {

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
    // CP-04-EXT-01: Fecha de hoy (límite superior válido de fecha)
    // Criterio HU-04: "La fecha no puede ser futura" → hoy sí es válida
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-EXT-01: Fecha de hoy es válida – no debe ser bloqueada por la validación de fecha futura")
    void CP_04_EXT_01_registerExpense_todayDate_isAccepted() {
        // Arrange
        Expense expenseToday = Expense.builder()
                .userId(1L)
                .amount(new BigDecimal("150.00"))
                .description("Almuerzo")
                .date(LocalDate.now())          // exactamente hoy → límite válido
                .categoryId(20L)
                .build();

        Expense saved = Expense.builder()
                .id(7L)
                .userId(1L)
                .amount(new BigDecimal("150.00"))
                .description("Almuerzo")
                .date(LocalDate.now())
                .categoryId(20L)
                .categoryName("Alimentación")
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryGateway.existsById(20L)).thenReturn(true);
        when(expenseGateway.save(any(Expense.class))).thenReturn(saved);

        // Act
        Expense result = expenseUseCase.registerExpense(expenseToday);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getDate()).isEqualTo(LocalDate.now());
        verify(expenseGateway).save(expenseToday);
    }

    // ─────────────────────────────────────────────
    // CP-04-EXT-02: Mañana como fecha futura (límite inferior inválido)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-EXT-02: Mañana (now+1) es rechazado – límite exacto de fecha futura")
    void CP_04_EXT_02_registerExpense_tomorrowDate_throwsInvalidExpenseException() {
        // Arrange
        Expense expenseTomorrow = Expense.builder()
                .userId(1L)
                .amount(new BigDecimal("100.00"))
                .description("Pago por adelantado")
                .date(LocalDate.now().plusDays(1))  // mañana → inválido
                .categoryId(20L)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> expenseUseCase.registerExpense(expenseTomorrow))
                .isInstanceOf(InvalidExpenseException.class)
                .hasMessage(DomainConstants.DATE_CANNOT_BE_FUTURE);

        verifyNoInteractions(expenseGateway);
    }

    // ─────────────────────────────────────────────
    // CP-04-EXT-03: Monto mínimo válido (0.01)
    // La validación exige > 0, por lo que 0.01 debe pasar
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-EXT-03: Monto mínimo (0.01) es aceptado – valor límite positivo")
    void CP_04_EXT_03_registerExpense_minimumValidAmount_persistsSuccessfully() {
        // Arrange
        Expense expenseMinAmount = Expense.builder()
                .userId(1L)
                .amount(new BigDecimal("0.01"))
                .description("Propina mínima")
                .date(LocalDate.now())
                .categoryId(20L)
                .build();

        Expense saved = Expense.builder()
                .id(11L)
                .userId(1L)
                .amount(new BigDecimal("0.01"))
                .date(LocalDate.now())
                .categoryId(20L)
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryGateway.existsById(20L)).thenReturn(true);
        when(expenseGateway.save(any(Expense.class))).thenReturn(saved);

        // Act
        Expense result = expenseUseCase.registerExpense(expenseMinAmount);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
        verify(expenseGateway).save(expenseMinAmount);
    }

    // ─────────────────────────────────────────────
    // CP-04-EXT-04: La validación de monto ocurre antes que la de fecha
    // Garantiza el orden interno de validateExpense()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-EXT-04: Monto inválido con fecha futura – el error de monto tiene precedencia")
    void CP_04_EXT_04_registerExpense_invalidAmountAndFutureDate_amountErrorHasPrecedence() {
        // Arrange – ambas validaciones fallidas: monto 0 + fecha futura
        Expense doubleInvalidExpense = Expense.builder()
                .userId(1L)
                .amount(BigDecimal.ZERO)
                .description("Error doble")
                .date(LocalDate.now().plusDays(5))
                .categoryId(20L)
                .build();

        // Act & Assert – la excepción lanzada debe ser la de monto (primera validación)
        assertThatThrownBy(() -> expenseUseCase.registerExpense(doubleInvalidExpense))
                .isInstanceOf(InvalidExpenseException.class)
                .hasMessage(DomainConstants.AMOUNT_MUST_BE_POSITIVE);

        verifyNoInteractions(expenseGateway);
        verifyNoInteractions(categoryGateway);
    }

    // ─────────────────────────────────────────────
    // CP-04-EXT-05: Fecha en el pasado lejano es válida
    // El dominio solo restringe fechas futuras, no pasadas
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-EXT-05: Fecha del pasado lejano es aceptada – no hay restricción de antigüedad")
    void CP_04_EXT_05_registerExpense_oldPastDate_isAccepted() {
        // Arrange
        Expense oldExpense = Expense.builder()
                .userId(1L)
                .amount(new BigDecimal("50.00"))
                .description("Gasto histórico")
                .date(LocalDate.of(2020, 1, 1))  // fecha muy antigua
                .categoryId(20L)
                .build();

        Expense saved = Expense.builder()
                .id(3L)
                .userId(1L)
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.of(2020, 1, 1))
                .categoryId(20L)
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryGateway.existsById(20L)).thenReturn(true);
        when(expenseGateway.save(any(Expense.class))).thenReturn(saved);

        // Act
        Expense result = expenseUseCase.registerExpense(oldExpense);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        verify(expenseGateway).save(oldExpense);
    }

    // ─────────────────────────────────────────────
    // CP-04-EXT-06: getAllExpenses con múltiples registros retorna la lista completa
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-EXT-06: getAllExpenses retorna todos los gastos sin filtrar por mes")
    void CP_04_EXT_06_getAllExpenses_multipleRecords_returnsAll() {
        // Arrange
        List<Expense> allExpenses = List.of(
                Expense.builder().id(1L).userId(1L).amount(new BigDecimal("100.00"))
                        .date(LocalDate.of(2024, 1, 10)).categoryId(20L).build(),
                Expense.builder().id(2L).userId(1L).amount(new BigDecimal("200.00"))
                        .date(LocalDate.of(2024, 3, 5)).categoryId(20L).build(),
                Expense.builder().id(3L).userId(1L).amount(new BigDecimal("350.00"))
                        .date(LocalDate.of(2024, 5, 20)).categoryId(20L).build()
        );
        when(expenseGateway.findByUserId(1L)).thenReturn(allExpenses);

        // Act
        List<Expense> result = expenseUseCase.getAllExpenses(1L);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(allExpenses);
        verify(expenseGateway).findByUserId(1L);
        verifyNoInteractions(categoryGateway);
    }

    // ─────────────────────────────────────────────
    // CP-04-EXT-07: El orden de validaciones es monto → fecha → categoría
    // Si la categoría no existe pero el monto es 0, el error es de monto
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-04-EXT-07: Con categoría inexistente y monto válido – error es de categoría")
    void CP_04_EXT_07_registerExpense_validAmountButCategoryNotFound_categoryErrorThrown() {
        // Arrange
        when(categoryGateway.existsById(20L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> expenseUseCase.registerExpense(validExpense))
                .isInstanceOf(InvalidExpenseException.class)
                .hasMessage(DomainConstants.CATEGORY_NOT_FOUND);

        verify(categoryGateway).existsById(20L);
        verify(expenseGateway, never()).save(any());
    }
}