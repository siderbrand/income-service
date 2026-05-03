package com.udea.incomeservice.domain.exception;

public final class DomainConstants {

    private DomainConstants() {}

    public static final String AMOUNT_MUST_BE_POSITIVE = "El monto debe ser mayor a cero";
    public static final String CATEGORY_REQUIRED = "Debes seleccionar una categoría";
    public static final String DATE_REQUIRED = "La fecha es requerida";
    public static final String DESCRIPTION_REQUIRED = "La descripción es requerida";
    public static final String DATE_CANNOT_BE_FUTURE = "La fecha no puede ser futura";
    public static final String DUPLICATE_CATEGORY = "Ya existe una categoría con ese nombre";
    public static final String CATEGORY_NAME_REQUIRED = "El nombre de la categoría es requerido";
    public static final String CATEGORY_TYPE_INVALID = "El tipo debe ser INCOME o EXPENSE";
    public static final String CATEGORY_NOT_FOUND = "La categoría seleccionada no existe";
    public static final String BUDGET_ALREADY_EXISTS = "Ya existe un presupuesto para esta categoría";
    public static final String BUDGET_WARNING = "Estás cerca del límite en la categoría %s";
    public static final String BUDGET_EXCEEDED = "Presupuesto agotado";
    public static final String BALANCE_NEGATIVE_ALERT = "Tus gastos superan tus ingresos este mes";
}
