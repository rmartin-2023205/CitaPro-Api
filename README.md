# CitaPro - API Backend

Backend REST para la plataforma CitaPro, construido con Java 21, Spring Boot 3 y PostgreSQL con migraciones Flyway.

## Stack Tecnológico
- **Java 21 LTS**
- **Spring Boot 3.3.4**
  - Spring Web (API REST con prefijo `/api/v1`)
  - Spring Security (Autenticación y autorización basada en cookies HttpOnly y roles `ADMIN`, `PROFESSIONAL`, `CLIENT`)
  - Spring Data JPA + Hibernate
  - Jakarta Validation
  - Spring Boot Actuator
- **PostgreSQL 16** con extensión `btree_gist` para prevención de solapamientos concurrentes
- **Flyway** para migraciones versionadas de base de datos
- **SpringDoc OpenAPI (Swagger)**

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

## Pruebas
```bash
.\mvnw.cmd test
```
