# Frontend Architecture

## Objetivo

El frontend de NeuroInventory proporciona la interfaz de usuario para la gestión del inventario y el acceso a las capacidades inteligentes de la plataforma.

Su diseño busca ofrecer una experiencia de usuario moderna, modular y mantenible, separando la lógica de presentación, el estado de la aplicación y la comunicación con los servicios backend.

La aplicación se desarrolla con **Angular** y consume las APIs expuestas por los distintos servicios del sistema.

---

# Objetivos de Diseño

La arquitectura del frontend persigue los siguientes objetivos:

* Mantener una estructura modular y escalable.
* Separar la lógica de negocio de la presentación.
* Centralizar la comunicación con los servicios backend.
* Facilitar la reutilización de componentes.
* Gestionar el estado de la aplicación de forma predecible.
* Integrarse con los mecanismos de autenticación empresarial.

---

# Arquitectura General

La aplicación se organiza en módulos funcionales que representan capacidades del negocio.

```text
frontend/

├── core/
│   ├── models/        # Tipados compartidos (incluye los modelos de reportes)
│   ├── services/      # Clientes HTTP centralizados (Product, Report, etc.)
│   └── utils/         # Utilidades reutilizables (exportación CSV)
│
├── shared/
│
├── features/
│   ├── authentication/
│   ├── dashboard/     # Panel de análisis con KPIs y gráficos SVG nativos
│   │   └── components/
│   │       ├── kpi-card/
│   │       ├── donut-chart/
│   │       ├── line-chart/
│   │       └── bar-chart/
│   ├── products/
│   ├── categories/
│   ├── inventory/
│   ├── ai-assistant/
│   └── reports/       # Reportes avanzados con filtros y exportación CSV
│
├── layout/
│
└── assets/
```

---

# Capas de la Aplicación

La arquitectura distingue cuatro responsabilidades principales.

## Presentation

Contiene los componentes responsables de la interacción con el usuario.

Incluye:

* Pantallas.
* Componentes visuales.
* Formularios.
* Tablas.
* Diálogos.
* Navegación.

Los componentes deben contener la menor cantidad posible de lógica de negocio.

---

## Application

Coordina la interacción entre la interfaz y los servicios.

Responsabilidades:

* Gestión del estado.
* Orquestación de acciones del usuario.
* Validaciones de interfaz.
* Navegación.

Esta capa actúa como intermediaria entre la presentación y los servicios.

---

## Services

Encapsula toda la comunicación con el backend.

Cada servicio representa una API del sistema.

Ejemplos:

* ProductService
* CategoryService
* InventoryService
* ReportService
* AuthenticationService
* AIService

Ningún componente debe realizar llamadas HTTP directamente.

---

## Infrastructure

Incluye los elementos técnicos necesarios para el funcionamiento de la aplicación.

Ejemplos:

* HttpClient.
* Interceptores.
* Guards.
* Configuración.
* Manejo global de errores.
* Integración con Keycloak.

---

# Organización por Funcionalidades

La aplicación adopta una organización basada en funcionalidades (feature-based architecture).

Cada módulo encapsula:

* Componentes.
* Servicios específicos.
* Modelos.
* Rutas.
* Recursos propios.

Ejemplo:

```text
features/

└── products/

    ├── pages/
    ├── components/
    ├── services/
    ├── models/
    ├── routes.ts
    └── product.module.ts
```

Este enfoque facilita el mantenimiento y reduce el acoplamiento entre módulos.

---

# Comunicación con el Backend

Toda interacción con el sistema se realiza mediante APIs REST.

```mermaid
flowchart LR

UI[Angular Components]

STATE[Application Layer]

SERVICE[HTTP Services]

API[Inventory Service]

AI[AI Service]

UI --> STATE

STATE --> SERVICE

SERVICE --> API

SERVICE --> AI
```

---

# Autenticación

La autenticación se basa en OAuth2 y OpenID Connect mediante Keycloak.

El flujo es el siguiente:

```mermaid
sequenceDiagram

actor User

participant Frontend

participant Keycloak

participant Inventory

User->>Frontend: Acceder a la aplicación

Frontend->>Keycloak: Solicitar autenticación

Keycloak-->>Frontend: Access Token (JWT)

Frontend->>Inventory: Request + JWT

Inventory-->>Frontend: Respuesta
```

El token es enviado automáticamente en cada solicitud mediante un interceptor HTTP.

---

# Gestión del Estado

El estado de la aplicación se divide en dos categorías.

## Estado Global

Información compartida por toda la aplicación.

Ejemplos:

* Usuario autenticado.
* Roles.
* Configuración.
* Preferencias.

---

## Estado Local

Información específica de cada funcionalidad.

Ejemplos:

* Formulario de productos.
* Filtros de búsqueda.
* Datos de una tabla.
* Paginación.
* Filtros y vistas del dashboard o de los reportes avanzados.

Esta separación evita dependencias innecesarias entre módulos.

---

## Panel de Análisis y Reportes Avanzados

La funcionalidad analítica se implementa siguiendo la organización feature-based:

* **Dashboard** (`features/dashboard`): pantalla principal con KPIs del inventario, flujo de movimientos
  por período, valoración por categoría, productos destacados y alertas de stock bajo. Los gráficos
  (`donut-chart`, `line-chart`, `bar-chart`) están implementados con **SVG nativo**, sin dependencias
  externas de visualización, y siguen el patrón de componentes standalone con signals.
* **Reportes** (`features/reports`): interfaz tabulada con filtros avanzados (período, granularidad,
  tipo de movimiento, categoría, métrica y límite), tablas de detalle y exportación a CSV mediante
  `core/utils/csv-export.ts`, que neutraliza fórmulas para evitar inyección de código en hojas de cálculo.
* **Servicios**: `ReportService` (`core/services`) centraliza las llamadas a `/reports/dashboard`,
  `/reports/valuation/categories`, `/reports/movements/timeseries`, `/reports/movements/detail`,
  `/reports/products/top` y `/reports/stock/low`.
* **Modelos**: los tipos de respuesta se definen en `core/models/report.model.ts` reflejando los DTOs
  expuestos por el backend.

La navegación incorpora los accesos **Panel de análisis** y **Reportes** en la barra lateral, y la ruta
por defecto de la aplicación pasa a ser `/dashboard`.

---

# Integración con IA

El frontend proporciona una experiencia unificada para las funcionalidades inteligentes de la plataforma.

Entre ellas:

* Búsqueda semántica de productos.
* Chat asistido por IA.
* Consulta de documentación mediante RAG.
* Recomendaciones contextuales.

Desde la perspectiva del usuario, estas capacidades forman parte del flujo normal de trabajo, aunque internamente sean atendidas por el AI Service.

---

# Manejo de Errores

La aplicación implementa un manejo centralizado de errores para:

* Errores de autenticación.
* Errores de autorización.
* Errores de validación.
* Errores de red.
* Errores inesperados del servidor.

El objetivo es ofrecer mensajes claros al usuario y mantener una experiencia consistente.

---

# Escalabilidad

La arquitectura del frontend está preparada para incorporar nuevas funcionalidades sin afectar los módulos existentes.

Entre las capacidades previstas se encuentran:

* Gestión de múltiples almacenes.
* Notificaciones en tiempo real.
* Dashboards personalizados.
* Integración con agentes de IA.

La organización modular facilita el crecimiento de la aplicación manteniendo una base de código coherente y de fácil mantenimiento.
