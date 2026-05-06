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

@ExtendWith(MockitoExtension.class)
@DisplayName("CP-03: Casos de prueba - IncomeUseCase")
class IncomeUseCaseTest {

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
                .date(LocalDate.of(2024, 5, 1))
                .categoryId(10L)
                .build();
    }

    // ─────────────────────────────────────────────
    // CP-03-01: Registro exitoso de ingreso
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-01: Registro exitoso de ingreso – 201 Created (dominio)")
    void CP_03_01_registerIncome_happyPath_persistsAllFields() {
        // Arrange
        Income saved = Income.builder()
                .id(1L)
                .userId(validIncome.getUserId())
                .amount(validIncome.getAmount())
                .description(validIncome.getDescription())
                .date(validIncome.getDate())
                .categoryId(validIncome.getCategoryId())
                .categoryName("Trabajo")
                .createdAt(LocalDateTime.now())
                .build();

        when(categoryGateway.existsById(validIncome.getCategoryId())).thenReturn(true);
        when(incomeGateway.save(any(Income.class))).thenReturn(saved);

        // Act
        Income result = incomeUseCase.registerIncome(validIncome);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getDescription()).isEqualTo("Salario mensual");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2024, 5, 1));
        assertThat(result.getCategoryId()).isEqualTo(10L);

        verify(categoryGateway).existsById(validIncome.getCategoryId());
        verify(incomeGateway).save(validIncome);
    }

    // ─────────────────────────────────────────────
    // CP-03-02: Registro con monto inválido (<= 0)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-02: Monto inválido (cero) lanza InvalidIncomeException – 400 Bad Request")
    void CP_03_02_registerIncome_zeroAmount_throwsInvalidIncomeException() {
        // Arrange
        Income incomeWithZeroAmount = Income.builder()
                .userId(1L)
                .amount(BigDecimal.ZERO)
                .description("Bono")
                .date(LocalDate.now())
                .categoryId(10L)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> incomeUseCase.registerIncome(incomeWithZeroAmount))
                .isInstanceOf(InvalidIncomeException.class)
                .hasMessage(DomainConstants.AMOUNT_MUST_BE_POSITIVE);

        verifyNoInteractions(incomeGateway);
    }

    @Test
    @DisplayName("CP-03-02: Monto inválido (negativo) lanza InvalidIncomeException – 400 Bad Request")
    void CP_03_02_registerIncome_negativeAmount_throwsInvalidIncomeException() {
        // Arrange
        Income incomeWithNegativeAmount = Income.builder()
                .userId(1L)
                .amount(new BigDecimal("-100.00"))
                .description("Ajuste")
                .date(LocalDate.now())
                .categoryId(10L)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> incomeUseCase.registerIncome(incomeWithNegativeAmount))
                .isInstanceOf(InvalidIncomeException.class)
                .hasMessage(DomainConstants.AMOUNT_MUST_BE_POSITIVE);

        verifyNoInteractions(incomeGateway);
    }

    // ─────────────────────────────────────────────
    // CP-03-03: Registro sin categoría válida
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-03: Categoría inexistente lanza InvalidIncomeException – 400 Bad Request")
    void CP_03_03_registerIncome_categoryNotFound_throwsInvalidIncomeException() {
        // Arrange
        when(categoryGateway.existsById(validIncome.getCategoryId())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> incomeUseCase.registerIncome(validIncome))
                .isInstanceOf(InvalidIncomeException.class)
                .hasMessage(DomainConstants.CATEGORY_NOT_FOUND);

        verify(categoryGateway).existsById(validIncome.getCategoryId());
        verifyNoInteractions(incomeGateway);
    }
    // ─────────────────────────────────────────────
    // CP-03-04 (adicional): Categoría inexistente bloquea el guardado
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-04: categoryGateway verifica existencia antes de persistir")
    void CP_03_04_registerIncome_validAmount_categoryNotFound_neverCallsSave() {
        // Arrange
        when(categoryGateway.existsById(validIncome.getCategoryId())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> incomeUseCase.registerIncome(validIncome))
                .isInstanceOf(InvalidIncomeException.class)
                .hasMessage(DomainConstants.CATEGORY_NOT_FOUND);

        verify(categoryGateway).existsById(validIncome.getCategoryId());
        verify(incomeGateway, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // CP-03-05: Consulta de ingresos mensuales
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-05: Consulta mensual retorna lista del gateway para año y mes dados")
    void CP_03_05_getMonthlyIncomes_delegatesToGateway_returnsResult() {
        // Arrange
        List<Income> expected = List.of(
                Income.builder().id(1L).userId(1L).amount(new BigDecimal("500.00"))
                        .date(LocalDate.of(2024, 5, 1)).categoryId(10L).build(),
                Income.builder().id(2L).userId(1L).amount(new BigDecimal("200.00"))
                        .date(LocalDate.of(2024, 5, 15)).categoryId(10L).build()
        );
        when(incomeGateway.findByUserIdAndMonth(1L, 2024, 5)).thenReturn(expected);

        // Act
        List<Income> result = incomeUseCase.getMonthlyIncomes(1L, 2024, 5);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expected);
        verify(incomeGateway).findByUserIdAndMonth(1L, 2024, 5);
    }

    @Test
    @DisplayName("CP-03-05: Consulta mensual sin ingresos retorna lista vacía")
    void CP_03_05_getMonthlyIncomes_noResults_returnsEmptyList() {
        // Arrange
        when(incomeGateway.findByUserIdAndMonth(1L, 2024, 5)).thenReturn(List.of());

        // Act
        List<Income> result = incomeUseCase.getMonthlyIncomes(1L, 2024, 5);

        // Assert
        assertThat(result).isEmpty();
        verify(incomeGateway).findByUserIdAndMonth(1L, 2024, 5);
    }

    // ─────────────────────────────────────────────
    // CP-03-06: Consulta de todos los ingresos del usuario
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("CP-03-06: getAllIncomes delega al gateway y retorna todos los registros del usuario")
    void CP_03_06_getAllIncomes_delegatesToGateway_returnsAllRecords() {
        // Arrange
        List<Income> expected = List.of(
                Income.builder().id(1L).userId(1L).amount(new BigDecimal("500.00"))
                        .date(LocalDate.of(2024, 4, 1)).categoryId(10L).build(),
                Income.builder().id(2L).userId(1L).amount(new BigDecimal("300.00"))
                        .date(LocalDate.of(2024, 5, 1)).categoryId(10L).build()
        );
        when(incomeGateway.findByUserId(1L)).thenReturn(expected);

        // Act
        List<Income> result = incomeUseCase.getAllIncomes(1L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expected);
        verify(incomeGateway).findByUserId(1L);
    }
}
