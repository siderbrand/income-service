package com.udea.incomeservice.infrastructure.entrypoint.rest;

public final class EntryPointConstants {

    private EntryPointConstants() {}

    public static final String INCOME_BASE_PATH = "/api/incomes";
    public static final String EXPENSE_BASE_PATH = "/api/expenses";
    public static final String USER_PATH = "/user/{userId}";
    public static final String USER_MONTHLY_PATH = "/user/{userId}/monthly";

    public static final String AMOUNT_REQUIRED = "El monto es requerido";
    public static final String AMOUNT_MUST_BE_POSITIVE = "El monto debe ser mayor a cero";
    public static final String AMOUNT_MIN = "0.01";
    public static final String DESCRIPTION_REQUIRED = "La descripción es requerida";
    public static final int DESCRIPTION_MAX_LENGTH = 255;
    public static final String DATE_REQUIRED = "La fecha es requerida";
    public static final String CATEGORY_REQUIRED = "Debes seleccionar una categoría";
    public static final String AMOUNT_FIELD_REQUIRED = "El campo monto es obligatorio";
    public static final String DATE_CANNOT_BE_FUTURE = "La fecha no puede ser futura";
    public static final String VALIDATION_ERROR = "Error de validación";
    public static final String INTERNAL_SERVER_ERROR = "Error interno del servidor";
}
