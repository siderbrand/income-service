package com.udea.incomeservice.domain.exception;

public class InvalidIncomeException extends RuntimeException {
    public InvalidIncomeException(String message) {
        super(message);
    }
}