# Income Service 💰

Microservicio REST para la gestión de ingresos financieros, desarrollado con Spring Boot siguiendo arquitectura hexagonal.

## Tech Stack

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.5 |
| Spring Security | JWT (stateless) |
| Spring Data JPA | Incluido |
| PostgreSQL | 15+ |
| Lombok | 1.18.36 |
| JJWT | 0.12.6 |
| SpringDoc OpenAPI | 3.0.2 |

## Arquitectura Hexagonal (Scaffold Bancolombia)

```
com.udea.incomeservice/
│
├── application/                        → Capa de aplicación
│   └── config/                         → Configuración de beans y OpenAPI
│
├── domain/                             → Capa de dominio (lógica de negocio pura)
│   ├── model/                          → Entidades de dominio (POJOs)
│   ├── usecase/                        → Casos de uso (lógica de negocio)
│   ├── gateway/                        → Puertos de salida (interfaces)
│   └── exception/                      → Excepciones de dominio
│
└── infrastructure/                     → Capa de infraestructura
    ├── driven/                         → Adaptadores de salida (driven adapters)
    │   ├── persistence/
    │   │   ├── entity/                 → Entidades JPA
    │   │   ├── repository/             → Repositorios Spring Data
    │   │   ├── mapper/                 → Mappers Entity ↔ Model
    │   │   └── adapter/               → Implementación de gateways
    │   └── security/                   → Seguridad JWT
    │       ├── JwtProvider             → Parseo y validación de tokens
    │       ├── JwtAuthenticationFilter → Filtro HTTP de autenticación
    │       └── SecurityConfig          → Configuración Spring Security
    │
    └── entrypoint/                     → Adaptadores de entrada (driving adapters)
        └── rest/
            ├── controller/             → REST Controllers
            ├── dto/                    → Request/Response DTOs
            ├── mapper/                 → Mappers DTO ↔ Model
            └── handler/               → Manejo global de excepciones
```

### Principios de la arquitectura

- **Dominio independiente**: No tiene dependencias de Spring ni de infraestructura
- **Puertos y adaptadores**: Los gateways (interfaces) definen contratos que la infraestructura implementa
- **Inyección de dependencias**: Los use cases se configuran como beans en la capa de aplicación
- **Seguridad como adaptador driven**: La validación JWT es un detalle de infraestructura

## Autenticación

La autenticación se gestiona en el microservicio `financial-management` (puerto 8080). Este servicio **valida el token JWT** que viene en el header `Authorization: Bearer <token>`.

El token JWT debe contener el claim `userId` con el ID del usuario autenticado. El `userId` se extrae automáticamente del token, no se envía en el body del request.

> **Importante:** Ambos microservicios deben compartir la misma clave secreta JWT (`JWT_SECRET`).

## API Endpoints

Todos los endpoints (excepto Swagger) requieren header `Authorization: Bearer <token>`.

| Método | Ruta | Descripción | Response |
|--------|------|-------------|----------|
| POST | `/api/incomes` | Registrar un ingreso | `201 Created` |
| GET | `/api/incomes/user/{userId}` | Listar todos los ingresos de un usuario | `200 OK` |
| GET | `/api/incomes/user/{userId}/monthly?year=&month=` | Ingresos del mes de un usuario | `200 OK` |

### Ejemplo de request — Registrar ingreso

```bash
curl -X POST http://localhost:8081/api/incomes \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 2500000,
    "description": "Salario mensual",
    "date": "2025-01-15",
    "category": "Salario"
  }'
```

### Ejemplo de response

```json
{
  "id": 1,
  "userId": 5,
  "amount": 2500000,
  "description": "Salario mensual",
  "date": "2025-01-15",
  "category": "Salario",
  "createdAt": "2025-01-15T10:30:00"
}
```

### Errores de validación

| Caso | Status | Mensaje |
|------|--------|---------|
| Monto ≤ 0 | `400` | El monto debe ser mayor a cero |
| Categoría vacía | `400` | Debes seleccionar una categoría |
| Sin token / token inválido | `401` | Unauthorized |

## Configuración

### Variables de entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `DB_URL` | URL de conexión PostgreSQL | `jdbc:postgresql://localhost:5432/income_db` |
| `DB_USER` | Usuario de la BD | `postgres` |
| `DB_PASSWORD` | Contraseña de la BD | `postgres` |
| `JWT_SECRET` | Clave secreta JWT (Base64, compartida con financial-management) | — |

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
export DB_PASSWORD=<password>
export JWT_SECRET=<clave-base64-compartida-con-financial-management>

# Ejecutar
./mvnw spring-boot:run
```

## Documentación Swagger

Con la aplicación corriendo:

- Swagger UI: http://localhost:8081/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

> Usa el botón **Authorize** en Swagger UI para ingresar el token JWT antes de probar los endpoints.

## Relación con otros microservicios

| Microservicio | Puerto | Descripción |
|---------------|--------|-------------|
| financial-management | 8080 | Gestión de usuarios y autenticación (emite JWT) |
| income-service | 8081 | Gestión de ingresos (valida JWT) |
