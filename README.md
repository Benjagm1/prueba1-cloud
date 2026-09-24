# Libro de Clases Digital — Colegio Bernardo O'Higgins (Coquimbo)

Plataforma productiva de **libro de clases electrónico** con arquitectura de **microservicios** (Java 25 / Spring Boot 4), **MySQL 8.0** (database-per-service), frontend **Angular 19** y despliegue **Docker Compose** con límites de CPU/RAM.

Cumple controles de **JWT**, **auditoría de eventos** (trazabilidad Ley 21.719 — datos estudiantiles) y **circuit breaker** en llamadas entre servicios.

---

## Arquitectura

```text
                    ┌─────────────────┐
                    │  Frontend SPA   │
                    │  (Angular/nginx)│
                    └────────┬────────┘
                             │ HTTP
     ┌───────────────────────┼───────────────────────┐
     ▼                       ▼                       ▼
┌─────────┐           ┌─────────────┐          ┌────────────┐
│ Login   │◄─────────►│Administración│◄────────►│ Mensajería │
│ :8051   │  internal │   :8052     │ internal │   :8056    │
└────┬────┘           └──────┬──────┘          └────────────┘
     │                       │
     │                  permisos/JWT
     │                       │
     │              ┌────────┴────────┐
     │              ▼                 ▼
     │        ┌──────────┐      ┌────────────┐
     │        │ Académico│      │ Asistencia │
     │        │  :8053   │      │   :8054    │
     │        └──────────┘      └────────────┘
     │
     ▼
┌──────────────────────────────────────────────┐
│ MySQL 8.0 — esquemas aislados por servicio   │
│ db_usuarios | db_administracion | db_academico│
│ db_asistencia | db_mensajeria                 │
└──────────────────────────────────────────────┘
```

### Capas por microservicio (N-Tier)

| Capa | Responsabilidad |
|------|-----------------|
| **Controller** | Endpoints REST, validación HTTP, roles |
| **Service** | Reglas de negocio académicas |
| **Repository** | Persistencia JPA / transacciones |

### Resiliencia y seguridad transversal

| Mecanismo | Ubicación |
|-----------|-----------|
| **JWT** (HS256, claim `tipo`) | `ms-common-security` — misma `JWT_SECRET` en todos los MS |
| **Circuit Breaker** | `ms.common.resilience.CircuitBreaker` en clientes HTTP |
| **Auditoría** | Tabla `auditoria_eventos` en Administración vía `POST /internal/auditoria` |
| **Token interno** | Header `X-Internal-Token` en rutas `/internal/**` |

---

## Microservicios

| # | Servicio | Módulo Maven | Puerto | Base de datos |
|---|----------|--------------|--------|---------------|
| 1 | **Login** | `ms-autenticacion` | 8051 | `db_usuarios` |
| 2 | **Administración** | `ms-administracion` | 8052 | `db_administracion` |
| 3 | **Cursos / Notas** | `ms-academico` | 8053 | `db_academico` |
| 4 | **Asistencia** | `ms-asistencia` | 8054 | `db_asistencia` |
| 5 | **Conducta** | `ms-conducta` | 8055 | `db_conducta` |
| 6 | **Mensajería** | `ms-mensajeria` | 8056 | `db_mensajeria` |

> El catálogo de **cursos y asignaturas** vive en **Administración**. **Académico** gestiona solo **calificaciones**.

---

## Contratos REST principales

Todas las rutas protegidas requieren:

```http
Authorization: Bearer <JWT>
```

### 1. Login (`ms-autenticacion` — `/auth`)

#### `POST /auth/login`

**Request:**
```json
{
  "email": "profesor@boh.cl",
  "contrasena": "MiClave123!"
}
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 12,
  "email": "profesor@boh.cl",
  "tipo": "profesor"
}
```

#### `GET /auth/perfil` · `PUT /auth/perfil`

**GET Response:**
```json
{
  "idUsuario": 12,
  "nombre": "Ana",
  "apellido": "Pérez",
  "email": "profesor@boh.cl",
  "telefono": "+56912345678",
  "tipo": "profesor"
}
```

**PUT Request:**
```json
{ "email": "nuevo@boh.cl", "telefono": "+56987654321" }
```

#### `POST /auth/usuarios` (solo admin)

**Request:**
```json
{
  "nombre": "Carlos",
  "apellido": "Muñoz",
  "email": "carlos@boh.cl",
  "contrasena": "Temporal123!",
  "tipo": "profesor"
}
```

**Response:**
```json
{ "idUsuario": 15, "email": "carlos@boh.cl", "tipo": "profesor" }
```

> `POST /auth/registrar` está **deshabilitado** (403). Solo el administrador crea cuentas.

---

### 2. Administración (`ms-administracion`)

#### Catálogo y provisión (admin)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/admin/cursos` | Lista cursos |
| `GET` | `/api/admin/asignaturas` | Lista asignaturas |
| `POST` | `/api/admin/cuentas/provisionar-familia` | Crea alumno + apoderado + curso + vínculo |
| `POST` | `/api/admin/cuentas/provisionar` | Crea una cuenta suelta |
| `GET` | `/api/admin/docentes/{id}/asignaciones` | Carga docente |
| `POST` | `/api/admin/docentes/asignaciones` | Asigna curso + asignatura a profesor |

**POST `/api/admin/docentes/asignaciones`:**
```json
{
  "profesorUsuarioId": 12,
  "cursoId": 3,
  "asignaturaId": 1
}
```

#### Consultas docente

| Método | Ruta |
|--------|------|
| `GET` | `/api/docentes/mis-asignaciones` |
| `GET` | `/api/docentes/lista-alumnos?cursoId=&asignaturaId=` |
| `GET` | `/api/docentes/contactos-mensajeria` |

**Ejemplo `mis-asignaciones`:**
```json
[
  { "cursoId": 3, "cursoNombre": "5° Básico", "asignaturaId": 1, "asignaturaNombre": "Matemáticas" }
]
```

#### Consultas apoderado

| Método | Ruta |
|--------|------|
| `GET` | `/api/apoderados/mis-pupilos` |
| `GET` | `/api/apoderados/contactos-mensajeria` |

---

### 3. Académico / Notas (`ms-academico` — `/api/academico`)

| Método | Ruta | Rol |
|--------|------|-----|
| `GET` | `/mis-notas` | alumno |
| `GET` | `/alumnos/{id}/notas` | profesor, apoderado, admin |
| `POST` | `/calificaciones` | profesor |

**POST `/calificaciones`:**
```json
{
  "alumnoUsuarioId": 20,
  "cursoId": 3,
  "asignaturaId": 1,
  "nombreEvaluacion": "Control 1",
  "nota": 6.2
}
```

**Response:**
```json
{
  "id": 45,
  "alumnoUsuarioId": 20,
  "cursoId": 3,
  "asignaturaId": 1,
  "nombreEvaluacion": "Control 1",
  "nota": 6.2,
  "creadoEn": "2026-05-28T10:30:00Z"
}
```

---

### 4. Asistencia (`ms-asistencia` — `/api/asistencia`)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/dia?fecha=2026-05-28&cursoId=3&asignaturaId=1` | Consulta asistencia del día |
| `POST` | `/dia` | Guarda asistencia del día |
| `GET` | `/alumnos/{id}/historial?desde=&hasta=` | Historial alumno |
| `GET` | `/mi-resumen?desde=&hasta=` | Resumen % propio (alumno) |

**POST `/dia`:**
```json
{
  "fecha": "2026-05-28",
  "cursoId": 3,
  "asignaturaId": 1,
  "filas": [
    { "alumnoUsuarioId": 20, "presente": true },
    { "alumnoUsuarioId": 21, "presente": false }
  ]
}
```

**GET `/mi-resumen` Response:**
```json
{
  "totalClases": 40,
  "presentes": 36,
  "porcentaje": 90.0,
  "alertaBajo85": false
}
```

---

### 5. Mensajería (`ms-mensajeria` — `/api/mensajes`)

| Método | Ruta |
|--------|------|
| `POST` | `/enviar` |
| `GET` | `/recibidos` |
| `GET` | `/mis-notificaciones` |

**POST `/enviar`:**
```json
{
  "destinatarioUsuarioId": 8,
  "asunto": "Consulta evaluación",
  "cuerpo": "Estimado apoderado..."
}
```

---

## Frontend Angular (`frontend-bo`)

**UI oficial del repositorio GitHub:** `H/Stack/frontend/frontend-bo` (commit `origin/main` — portal con tarjetas de rol, login institucional, panel profesor con sidebar).

- **Origen:** [github.com/ctorresp/H](https://github.com/ctorresp/H) → carpeta `Stack/frontend/frontend-bo`
- **Integración backend:** JWT (`AuthService`), interceptor, guards, asistencia y login conectados a los microservicios
- **Docker:** el contenedor `frontend` construye **solo** `frontend-bo` (no la carpeta `Stack/frontend` raíz)

### Desarrollo local (sin Docker frontend)

Requiere los microservicios levantados (Docker o APIs en 8051–8056).

```powershell
cd "H\Stack\frontend\frontend-bo"
npm.cmd install
npm.cmd start
```

Abre **http://127.0.0.1:8050** — el proxy en `proxy.conf.json` redirige a los microservicios en `127.0.0.1`.

### Credenciales demo

| Rol | Email | Contraseña |
|-----|-------|------------|
| Admin | `admin@boh.cl` | `Admin123!` |

---

## Despliegue con Docker Compose

### Requisitos

- Docker Desktop 4.x+ (Compose V2)
- 4 GB RAM libres recomendados
- Puertos: `8050` (frontend), `8051–8056` (APIs), `3307` (MySQL)

### Paso a paso

```powershell
cd "H\Contenedores"

# Opcional: variables de entorno
copy .env.example .env
# Edite JWT_SECRET, contraseñas, etc.

docker compose down
docker compose up -d --build
```

### Verificar estado

```powershell
docker compose ps
docker compose logs -f frontend
```

### Acceso

Use **127.0.0.1** (no `localhost`) para evitar problemas de resolución en Windows:

| Servicio | URL |
|----------|-----|
| **Aplicación completa (frontend-bo)** | http://127.0.0.1:8050 |
| Auth API directa | http://127.0.0.1:8051 |
| MySQL | `127.0.0.1:3307` (root / ver `.env`) |

Verificación rápida:

```powershell
curl.exe -s http://127.0.0.1:8050/ | findstr "Libro de Clases"
curl.exe -s -X POST http://127.0.0.1:8050/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@boh.cl\",\"contrasena\":\"Admin123!\"}"
```

El contenedor **frontend** (nginx) actúa como **reverse proxy** del SPA hacia los 5 microservicios (`nginx.conf`).

### Límites de recursos (horas punta)

Configurados en `docker-compose.yml` con `deploy.resources.limits`:

| Contenedor | CPU máx. | RAM máx. |
|------------|----------|----------|
| MySQL | 1.0 | 1024 MB |
| Administración | 0.75 | 768 MB |
| Asistencia | 0.40 | 448 MB |
| Auth / Académico / Mensajería | 0.50 | 512 MB |
| Frontend | 0.25 | 128 MB |

Logs rotan con `max-size: 10m`, `max-file: 3`.

---

## Estructura del repositorio

```text
H/
├── Contenedores/
│   ├── docker-compose.yml      # Stack productivo
│   ├── .env.example
│   ├── mysql-init/
│   └── Contenedor - */         # Dockerfiles por servicio + frontend
├── Stack/
│   ├── Backend/                # Maven multi-módulo (5 MS + common)
│   └── frontend/               # Angular 19
└── README.md                   # Este archivo
```

---

## Flujo Git (Trunk-based)

1. Trabajar en ramas cortas desde `main`.
2. Integrar vía PR pequeños y revisables.
3. Desplegar desde `main` con `docker compose up -d --build`.

---

## Integración repositorio compañero

El frontend desplegado es **`frontend-bo`** del repositorio [ctorresp/H](https://github.com/ctorresp/H.git), con integración al backend:

- Landing con tarjetas de perfil (Alumno, Apoderado, Profesor, Administrador).
- Login institucional y panel profesor (asistencia, anotaciones, calificaciones).
- Autenticación JWT real y proxy nginx hacia los 5 microservicios.

---

## Normativa y datos sensibles

- Contraseñas con **BCrypt**.
- JWT con expiración configurable (`jwt.expiration`).
- **Auditoría** de login, calificaciones, asistencia y mensajes en `auditoria_eventos`.
- Registro público deshabilitado; provisión solo por administrador.
- En producción: cambiar `JWT_SECRET`, `MYSQL_ROOT_PASSWORD`, `INTERNAL_API_TOKEN` y `APP_ADMIN_PASSWORD` en `.env`.

---

## Solución de problemas

| Síntoma | Acción |
|---------|--------|
| APIs en `Restarting` | `docker compose logs auth-api` — espere healthcheck (~90s) |
| Frontend 502 | Verifique que todos los MS estén `healthy` |
| Login falla | Cuenta debe estar completa (alumno: curso+apoderado; profesor: ≥1 asignación) |
| Build Maven lento | Normal en primer `docker compose build` (~3–5 min) |

---

**Colegio Bernardo O'Higgins — Coquimbo** · Libro de Clases Digital · Java 25 · Spring Boot 4 · Angular 19
