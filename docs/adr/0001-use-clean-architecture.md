# ADR-0001: Adoptar Clean Architecture

## Estado

Aceptada

---

## Contexto

NeuroInventory requiere una arquitectura backend capaz de evolucionar desde una aplicación empresarial inicial hacia una plataforma con capacidades avanzadas de inteligencia artificial.

El sistema debe permitir:

* Evolución continua del dominio de inventario.
* Incorporación de nuevos servicios.
* Pruebas automatizadas.
* Sustitución de tecnologías de infraestructura.
* Mantener separadas las reglas de negocio y los detalles técnicos.

Una arquitectura tradicional basada directamente en controladores, servicios y repositorios puede generar un alto acoplamiento entre el negocio y frameworks específicos.

---

## Decisión

Se adopta **Clean Architecture** como patrón arquitectónico principal del backend.

La aplicación se organiza en capas:

```text
Domain

    ↓

Application

    ↓

Infrastructure
```

Las dependencias apuntan hacia el dominio, manteniendo las reglas de negocio independientes de:

* Spring Boot.
* Hibernate.
* PostgreSQL.
* APIs REST.
* Servicios externos.

---

## Alternativas consideradas

### Arquitectura MVC tradicional

**Descripción:**

Organizar la aplicación utilizando directamente:

```text
Controller

Service

Repository

Database
```

**Ventajas:**

* Implementación rápida.
* Fácil de entender inicialmente.

**Desventajas:**

* La lógica de negocio suele mezclarse con infraestructura.
* Mayor dificultad para pruebas unitarias.
* Mayor acoplamiento tecnológico.

---

### Arquitectura Hexagonal

**Descripción:**

Separar el dominio mediante puertos y adaptadores.

**Ventajas:**

* Excelente aislamiento del dominio.
* Muy cercana a Clean Architecture.

**Desventajas:**

* Requiere una estructura conceptual similar.
* La diferencia práctica para este proyecto es mínima.

---

## Consecuencias

### Positivas

* El dominio permanece independiente de frameworks.
* Mayor facilidad para realizar pruebas.
* Facilita la evolución del sistema.
* Permite reemplazar infraestructura sin modificar reglas de negocio.

### Negativas

* Mayor cantidad de clases y abstracciones.
* Mayor esfuerzo inicial de diseño.
* Requiere disciplina arquitectónica.

---

## Referencias

* Robert C. Martin - Clean Architecture.
* Domain-Driven Design - Eric Evans.
