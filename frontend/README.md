# Frontend — NeuroInventory

Aplicación web del proyecto NeuroInventory construida con **Angular 22** y **Angular Material**.

Responsabilidades:

- Dashboard de inventario.
- Gestión CRUD de productos, categorías y stock.
- Login con Keycloak.
- Chat IA y búsqueda semántica.
- Visualización de stock.

## Prerrequisitos

- Node.js (versión compatible con Angular 22).
- npm 11.19.0 (definido como `packageManager` en `package.json`).
- Backend en ejecución para las funcionalidades completas:
  - `inventory-service` (Spring Boot) en `http://localhost:8080`.
  - Keycloak en `http://localhost:9090` (realm `neuroinventory`, client `frontend-client`).

## Configuración

Las URLs de los servicios se definen en `src/environments/environment.ts`:

```ts
apiUrl: 'http://localhost:8080/api/v1/inventory-service',
keycloak: { url, realm, clientId }
```

## Instalación

```bash
npm ci
```

## Servidor de desarrollo

```bash
npm start
```

Abre `http://localhost:4200/`. La app recarga automáticamente al modificar archivos.

## Build

```bash
npm run build
```

Los artefactos se generan en `dist/`. Para build de desarrollo con recarga:

```bash
npm run watch
```

## Tests

```bash
npm test
```

Ejecuta las pruebas unitarias con Vitest.

## Docker

```bash
docker build -t neuroinventory-frontend .
docker run -p 4200:4200 neuroinventory-frontend
```

También puedes levantarlo junto con el resto de servicios desde la raíz del repo:

```bash
docker compose up frontend
```

## Estructura

```text
src/app/
├── core/        # auth (Keycloak), interceptors, models, services
├── features/    # ai-assistant, categories, inventory, products
└── layout/      # layout principal de la app
```
