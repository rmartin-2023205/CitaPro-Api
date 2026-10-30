# CitaPro - API Backend

Backend REST para la plataforma CitaPro, construido con Java 21, Spring Boot 3 y PostgreSQL con migraciones Flyway.

## Stack Tecnológico
- **Java 21 LTS**
- **Spring Boot 3.3.4**
  - Spring Web (API REST con prefijo `/api/v1`)
  - Spring Security (Autenticación y autorización basada en cookies `HttpOnly` y roles `ADMIN`, `PROFESSIONAL`, `CLIENT`)
  - Spring Data JPA + Hibernate
  - Jakarta Validation + respuestas de error `RFC 7807 ProblemDetail`
  - Spring Boot Actuator
- **PostgreSQL 16** con extensión `btree_gist` para prevención de solapamientos concurrentes
- **Flyway** para migraciones versionadas de base de datos (`V1` a `V5`)
- **SpringDoc OpenAPI (Swagger)**

## Módulos y Endpoints Principales

| Módulo | Endpoints | Rol Requerido |
|--------|-----------|---------------|
| **Salud y Diagnóstico** | `GET /api/v1/health`, `GET /api/v1/actuator/health` | Público |
| **Catálogo Público** | `GET /api/v1/public/business`, `GET /api/v1/public/services`, `GET /api/v1/public/professionals` | Público |
| **Disponibilidad** | `GET /api/v1/public/availability?serviceId=...&professionalId=...&date=YYYY-MM-DD` | Público |
| **Autenticación** | `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/me` | Público / Autenticado |
| **Reservas (Cliente)** | `POST /api/v1/appointments`, `GET /api/v1/appointments/my`, `PATCH /api/v1/appointments/{id}/cancel` | `CLIENT`, `PROFESSIONAL`, `ADMIN` |
| **Agenda Profesional** | `GET /api/v1/professional/me`, `GET /api/v1/professional/agenda`, `PATCH /api/v1/appointments/{id}/status` | `PROFESSIONAL`, `ADMIN` |
| **Panel Administrativo** | `GET /api/v1/admin/dashboard/summary`, `GET /api/v1/admin/appointments`, CRUD `/api/v1/admin/services`, `/api/v1/admin/professionals`, `/api/v1/admin/blocks` | `ADMIN` |

## Puesta en marcha

### 1. Iniciar PostgreSQL
```bash
docker compose up -d
```

### 2. Configurar variables de entorno
```bash
cp .env.example .env
```

### 3. Ejecutar la aplicación
- **Windows:**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
- **Linux/macOS:**
  ```bash
  ./mvnw spring-boot:run
  ```

La API estará disponible en `http://localhost:8080/api/v1`.
- **Health Check:** `http://localhost:8080/api/v1/health`
- **Documentación Swagger UI:** `http://localhost:8080/api/v1/swagger-ui.html`

## Suite de Pruebas Automatizadas
El proyecto cuenta con **46 pruebas automatizadas** (JUnit 5 + Mockito + Jakarta Validation) que cubren:
- Catálogo público (`PublicCatalogServiceTest`)
- Autenticación, roles y sesiones (`AuthServiceTest`)
- Algoritmo de horarios, bloqueos y zona horaria (`AvailabilityServiceTest`)
- Creación transaccional de reservas, cancelación y concurrencia (`AppointmentServiceTest`)
- Paneles administrativos y KPIs (`AdminDashboardServiceTest`, `AdminManagementServiceTest`)
- Agenda del profesional (`ProfessionalAgendaServiceTest`)
- Traducción de errores a `ProblemDetail` (`GlobalExceptionHandlerTest`)
- Validación de DTOs y restricciones (`DtoValidationAndConcurrencyTest`)

```powershell
.\mvnw.cmd test
```
