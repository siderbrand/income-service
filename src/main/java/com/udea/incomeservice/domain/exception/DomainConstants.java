package com.udea.incomeservice.domain.exception;

public final class DomainConstants {

    private DomainConstants() {}

    public static final String AMOUNT_MUST_BE_POSITIVE = "El monto debe ser mayor a cero";
    public static final String CATEGORY_REQUIRED = "Debes seleccionar una categoría";
    public static final String DATE_REQUIRED = "La fecha es requerida";
    public static final String DESCRIPTION_REQUIRED = "La descripción es requerida";
    public static final String DATE_CANNOT_BE_FUTURE = "La fecha no puede ser futura";
}
