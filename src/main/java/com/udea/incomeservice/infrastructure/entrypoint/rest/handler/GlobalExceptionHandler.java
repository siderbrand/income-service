package com.udea.incomeservice.infrastructure.entrypoint.rest.handler;

import com.udea.incomeservice.domain.exception.DuplicateCategoryException;
import com.udea.incomeservice.domain.exception.InvalidExpenseException;
import com.udea.incomeservice.domain.exception.InvalidIncomeException;
import com.udea.incomeservice.infrastructure.entrypoint.rest.EntryPointConstants;
import com.udea.incomeservice.infrastructure.entrypoint.rest.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidIncomeException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidIncome(InvalidIncomeException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidExpenseException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidExpense(InvalidExpenseException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateCategory(DuplicateCategoryException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        String message = details.isEmpty() ? EntryPointConstants.VALIDATION_ERROR : details.getFirst();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDTO.builder()
                        .errorCode(HttpStatus.BAD_REQUEST.value())
                        .message(message)
                        .details(details)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneral(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, EntryPointConstants.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponseDTO> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ErrorResponseDTO.builder()
                        .errorCode(status.value())
                        .message(message)
                        .build());
    }
}
