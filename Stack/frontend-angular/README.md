# Libro de clases — Frontend Angular

Aplicación Angular 19 con patrón **Container / Presentational** (ej. `features/login/containers` + `presentational`).

## Desarrollo local

1. Levanta los microservicios (Docker Compose en `H/Contenedores`) en puertos **8051–8056**.
2. En esta carpeta:

```powershell
npm.cmd install
npm.cmd start
```

Abre **http://127.0.0.1:8050/** (proxy hacia los MS en `proxy.conf.json`).

## Cuentas

- Solo el **administrador** crea usuarios (`POST /api/admin/cuentas/provisionar` o panel Admin).
- Login: `admin@boh.cl` / `Admin123!` (si `APP_ADMIN_ENABLED=true` en Docker).

## Frontend React (legado)

El proyecto React en `H/Stack/frontend` sigue disponible; el frontend oficial nuevo es esta carpeta.
