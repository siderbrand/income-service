package com.udea.incomeservice.domain.usecase;

import com.udea.incomeservice.domain.exception.DomainConstants;
import com.udea.incomeservice.domain.exception.InvalidIncomeException;
import com.udea.incomeservice.domain.gateway.CategoryGateway;
import com.udea.incomeservice.domain.gateway.IncomeGateway;
import com.udea.incomeservice.domain.model.Income;
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
 * CP-03-EXT: Casos de prueba complementarios para IncomeUseCase.
 *
 * Estos tests amplían la cobertura de IncomeUseCaseTest cubriendo
 * valores límite (boundary), comportamiento con usuarios distintos
 * y verificaciones de interacción con los gateways.
 *
 * HU relacionada: HU-03 – Registro de Ingresos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CP-03-EXT: Casos límite y cobertura adicional – IncomeUseCase")
class IncomeUseCaseEdgeCaseTest {

    @Mock
    private IncomeGateway incomeGateway;

    @Mock
    private CategoryGateway categoryGateway;

    @InjectMocks
    private IncomeUseCase incomeUseCase;

    private Income validIncome;

    @BeforeEach
    void setUp() {
        validIncome = Income.builder()
                .userId(1L)
                .amount(new BigDecimal("500.00"))
                .description("Salario mensual")
                .date(LocalDate.now())
                .categoryId(10L)
                .build();
    }

    // ─────────────────────────────────────────────
    // CP-03-EXT-01: Valor límite inferior válido (0.01)
    // Criterio HU-03: El monto debe ser mayor a cero
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-EXT-01: Monto mínimo válido (0.01) – debe persistirse correctamente")
    void CP_03_EXT_01_registerIncome_minimumValidAmount_persistsSuccessfully() {
        // Arrange
        Income incomeWithMinAmount = Income.builder()
                .userId(1L)
                .amount(new BigDecimal("0.01"))
                .description("Centavo de ingreso")
                .date(LocalDate.now())
                .categoryId(10L)
                .build();

        Income saved = Income.builder()
                .id(99L)
                .userId(1L)
                .amount(new BigDecimal("0.01"))
                .description("Centavo de ingreso")
                .date(LocalDate.now())
                .categoryId(10L)
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryGateway.existsById(10L)).thenReturn(true);
        when(incomeGateway.save(any(Income.class))).thenReturn(saved);

        // Act
        Income result = incomeUseCase.registerIncome(incomeWithMinAmount);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
        verify(incomeGateway).save(incomeWithMinAmount);
    }

    // ─────────────────────────────────────────────
    // CP-03-EXT-02: Valor exactamente en cero (límite inválido)
    // Garantiza que el umbral de validación es estrictamente > 0
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-EXT-02: Monto 0.00 es rechazado – la validación es estrictamente mayor a cero")
    void CP_03_EXT_02_registerIncome_exactlyZero_throwsInvalidIncomeException() {
        // Arrange
        Income incomeAtZeroBoundary = Income.builder()
                .userId(1L)
                .amount(new BigDecimal("0.00"))
                .description("Sin valor")
                .date(LocalDate.now())
                .categoryId(10L)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> incomeUseCase.registerIncome(incomeAtZeroBoundary))
                .isInstanceOf(InvalidIncomeException.class)
                .hasMessage(DomainConstants.AMOUNT_MUST_BE_POSITIVE);

        verifyNoInteractions(incomeGateway);
    }

    // ─────────────────────────────────────────────
    // CP-03-EXT-03: Registro con fecha de hoy (valor límite temporal válido)
    // La HU no restringe fechas para ingresos, por lo que hoy debe ser válido
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-EXT-03: Fecha de hoy es válida para registro de ingreso")
    void CP_03_EXT_03_registerIncome_todayDate_isAccepted() {
        // Arrange
        Income incomeWithTodayDate = Income.builder()
                .userId(1L)
                .amount(new BigDecimal("1000.00"))
                .description("Pago del día")
                .date(LocalDate.now())
                .categoryId(10L)
                .build();

        Income saved = Income.builder()
                .id(5L)
                .userId(1L)
                .amount(new BigDecimal("1000.00"))
                .date(LocalDate.now())
                .categoryId(10L)
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryGateway.existsById(10L)).thenReturn(true);
        when(incomeGateway.save(any(Income.class))).thenReturn(saved);

        // Act
        Income result = incomeUseCase.registerIncome(incomeWithTodayDate);

        // Assert – la fecha de hoy no debe ser bloqueada
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
        verify(incomeGateway).save(incomeWithTodayDate);
    }

    // ─────────────────────────────────────────────
    // CP-03-EXT-04: Aislamiento de usuario – consulta mensual solo retorna
    // registros del userId correcto
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-EXT-04: getMonthlyIncomes solo consulta datos del userId solicitado")
    void CP_03_EXT_04_getMonthlyIncomes_delegatesCorrectUserId_gatewayCalledWithSameUserId() {
        // Arrange
        Long userId = 42L;
        when(incomeGateway.findByUserIdAndMonth(userId, 2024, 1)).thenReturn(List.of());

        // Act
        incomeUseCase.getMonthlyIncomes(userId, 2024, 1);

        // Assert – el gateway recibe exactamente el mismo userId sin transformación
        verify(incomeGateway).findByUserIdAndMonth(42L, 2024, 1);
        verifyNoMoreInteractions(incomeGateway);
    }

    // ─────────────────────────────────────────────
    // CP-03-EXT-05: La verificación de categoría ocurre ANTES de guardar
    // Garantiza el orden de las validaciones en validateIncome()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-EXT-05: La validación de categoría precede siempre al save del gateway")
    void CP_03_EXT_05_registerIncome_categoryCheckAlwaysBeforeSave() {
        // Arrange
        when(categoryGateway.existsById(10L)).thenReturn(true);
        when(incomeGateway.save(any())).thenReturn(validIncome);

        // Act
        incomeUseCase.registerIncome(validIncome);

        // Assert – el orden de invocaciones es: existsById → save
        var inOrder = inOrder(categoryGateway, incomeGateway);
        inOrder.verify(categoryGateway).existsById(10L);
        inOrder.verify(incomeGateway).save(validIncome);
    }

    // ─────────────────────────────────────────────
    // CP-03-EXT-06: getAllIncomes con lista vacía retorna colección vacía
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-EXT-06: getAllIncomes sin registros retorna lista vacía sin errores")
    void CP_03_EXT_06_getAllIncomes_noRecords_returnsEmptyList() {
        // Arrange
        when(incomeGateway.findByUserId(1L)).thenReturn(List.of());

        // Act
        List<Income> result = incomeUseCase.getAllIncomes(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(incomeGateway).findByUserId(1L);
        verifyNoInteractions(categoryGateway);
    }
}