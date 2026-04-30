# Income Service 💰

Microservicio REST para la gestión de ingresos financieros, desarrollado con Spring Boot siguiendo arquitectura hexagonal.

## Historia de Usuario

**HU-03:** Como usuario autenticado de la aplicación, quiero registrar mis ingresos indicando monto, descripción, fecha y categoría para llevar un control preciso de las fuentes de dinero que recibo mensualmente.

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Data JPA
- PostgreSQL
- Lombok
- SpringDoc OpenAPI (Swagger)

## Arquitectura Hexagonal

El proyecto sigue la arquitectura hexagonal (ports & adapters), organizada en tres capas:
    com.udea.incomeservice/
    │
    ├── application/                    → Capa de aplicación
    │   └── config/                     → Configuración de beans
    │
    ├── domain/                         → Capa de dominio (lógica de negocio)
    │   ├── model/                      → Entidades de dominio
    │   ├── usecase/                    → Casos de uso
    │   ├── gateway/                    → Puertos de salida (interfaces)
    │   └── exception/                  → Excepciones de dominio
    │
    └── infrastructure/                 → Capa de infraestructura
        ├── driven/                     → Adaptadores de salida
        │   └── persistence/
        │       ├── entity/             → Entidades JPA
        │       ├── repository/         → Repositorios Spring Data
        │       ├── mapper/             → Mappers Entity ↔ Model
        │       └── adapter/            → Implementación de gateways
        │
        └── entrypoint/                 → Adaptadores de entrada
            └── rest/
                ├── controller/         → REST Controllers
                ├── dto/                → Request/Response DTOs
                ├── mapper/             → Mappers DTO ↔ Model
                └── handler/           → Manejo global de excepciones


## API Endpoints

| Método | Ruta | Descripción | Response |
|--------|------|-------------|----------|
| POST | /api/incomes | Registrar un ingreso | 201 Created |
| GET | /api/incomes/user/{userId} | Listar todos los ingresos | 200 OK |
| GET | /api/incomes/user/{userId}/monthly?year=&month= | Ingresos del mes | 200 OK |

## Criterios de Aceptación Implementados

### ✅ Registro con datos completos
- Recibe monto, descripción, fecha y categoría
- Guarda el registro y lo retorna con ID generado

### ✅ Monto inválido
- Si el monto es cero o negativo retorna `400 Bad Request`
- Mensaje: `"El monto debe ser mayor a cero"`

### ✅ Sin categoría seleccionada
- Si la categoría está vacía retorna `400 Bad Request`
- Mensaje: `"Debes seleccionar una categoría"`

## Configuración

### Variables de entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| DB_URL | URL de PostgreSQL | jdbc:postgresql://localhost:5432/income_db |
| DB_USER | Usuario de la BD | postgres |
| DB_PASSWORD | Contraseña de la BD | postgres |

### Base de datos

```sql
CREATE DATABASE income_db;

CREATE TABLE incomes (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    amount      NUMERIC(15, 2)  NOT NULL CHECK (amount > 0),
    description VARCHAR(255)    NOT NULL,
    date        DATE            NOT NULL,
    category    VARCHAR(100)    NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);
```

## Ejecutar la aplicación

```bash
# Configurar variables de entorno
export DB_URL=jdbc:postgresql://localhost:5432/income_db
export DB_USER=postgres
export DB_PASSWORD= xxxxx

# Ejecutar
./mvnw spring-boot:run
```

## Documentación Swagger

Con la aplicación corriendo:

- Swagger UI: http://localhost:8081/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

## Relación con otros microservicios

Este microservicio es parte del sistema de gestión financiera:

| Microservicio | Puerto | Descripción |
|---------------|--------|-------------|
| financial-management | 8080 | Gestión de usuarios |
| income-service | 8081 | Gestión de ingresos |
