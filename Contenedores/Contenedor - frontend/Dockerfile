# Frontend oficial del repositorio (frontend-bo) — Colegio B. O'Higgins
FROM node:22-alpine AS builder
WORKDIR /app
COPY Stack/frontend/frontend-bo/package.json Stack/frontend/frontend-bo/package-lock.json ./
RUN npm ci
COPY Stack/frontend/frontend-bo .
RUN npm run build

FROM nginx:1.27-alpine
COPY ["Contenedores/Contenedor - frontend/nginx.conf", "/etc/nginx/conf.d/default.conf"]
COPY --from=builder /app/dist/frontend-bo/browser /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD wget -q --spider http://127.0.0.1/ || exit 1
