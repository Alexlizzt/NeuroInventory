# ADR-0005: Adoptar RFC 9457 para manejo de errores HTTP

## Estado

Aceptada

---

## Contexto

NeuroInventory expone APIs REST consumidas por diferentes componentes:

* Frontend Angular.
* Servicios internos.
* Futuras aplicaciones externas.
* Posibles agentes de inteligencia artificial mediante MCP.

Un sistema distribuido requiere una estrategia consistente para comunicar errores.

El formato estándar generado por frameworks puede variar dependiendo de la tecnología utilizada, dificultando:

* Interpretación de errores por clientes.
* Manejo uniforme en frontend.
* Documentación de APIs.
* Integración entre servicios.

La plataforma necesita un contrato de errores estable, independiente de la implementación interna.

---

# Decisión

Se adopta la especificación **RFC 9457 - Problem Details for HTTP APIs** como estándar para todas las respuestas de error HTTP.

Todas las respuestas de error utilizarán:

```http
Content-Type: application/problem+json
```

El formato base será:

```json
{
  "type": "https://api.neuroinventory.dev/problems/product-not-found",
  "title": "Product not found",
  "status": 404,
  "detail": "No product exists with identifier 15.",
  "instance": "/api/v1/products/15"
}
```

---

# Estructura del Error

Los campos estándar utilizados son:

| Campo      | Descripción                               |
| ---------- | ----------------------------------------- |
| `type`     | Identificador único del tipo de problema. |
| `title`    | Resumen legible del error.                |
| `status`   | Código HTTP asociado.                     |
| `detail`   | Descripción específica del problema.      |
| `instance` | Identificador de la solicitud afectada.   |

---

# Ejemplo de Error de Dominio

Cuando una regla de negocio impide una operación:

```json
{
  "type": "https://api.neuroinventory.dev/problems/insufficient-stock",
  "title": "Insufficient stock",
  "status": 409,
  "detail": "Cannot complete movement because available stock is 0.",
  "instance": "/api/v1/inventory/movements"
}
```

Los errores representan conceptos del dominio y no detalles internos de implementación.

---

# Extensiones Propias

RFC 9457 permite agregar propiedades adicionales cuando sea necesario.

Para errores de validación se utilizará una extensión `errors`.

Ejemplo:

```json
{
  "type": "https://api.neuroinventory.dev/problems/validation-error",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields contain invalid values.",
  "errors": {
    "name": [
      "must not be blank"
    ],
    "sku": [
      "already exists"
    ]
  }
}
```

Estas extensiones deben mantenerse documentadas y consistentes.

---

# Alternativas consideradas

## Formato de error por defecto de Spring Boot

Ejemplo:

```json
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "path": "/products/10"
}
```

### Ventajas

* Disponible automáticamente.
* No requiere configuración adicional.

### Desventajas

* Acoplado al framework.
* No representa conceptos del dominio.
* Puede cambiar entre versiones.
* No es ideal para consumidores externos.

---

## Crear formato propietario

Ejemplo:

```json
{
  "success": false,
  "code": "PRODUCT_NOT_FOUND",
  "message": "Product not found"
}
```

### Ventajas

* Total control del formato.
* Fácil de implementar.

### Desventajas

* No sigue estándares existentes.
* Requiere documentar un contrato propio.
* Menor interoperabilidad.

---

## Adoptar RFC 9457

### Ventajas

* Estándar internacional.
* Compatible con cualquier cliente HTTP.
* Independiente del lenguaje.
* Soportado por frameworks modernos.
* Facilita documentación OpenAPI.

### Desventajas

* Requiere definir convenciones internas para `type`.
* Necesita configuración inicial.

---

# Implementación en Spring Boot

Spring Framework proporciona soporte nativo mediante `ProblemDetail`.

Los errores serán centralizados mediante un manejador global:

```text
Exception

    |
    v

@RestControllerAdvice

    |
    v

ProblemDetail

    |
    v

application/problem+json
```

Ejemplo conceptual:

```java
@ExceptionHandler(ProductNotFoundException.class)
ProblemDetail handle(ProductNotFoundException ex) {

    ProblemDetail problem =
        ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

    problem.setTitle("Product not found");
    problem.setDetail(ex.getMessage());

    return problem;
}
```

---

# Impacto en la Arquitectura

Esta decisión establece un contrato transversal para todos los servicios:

```text
Frontend

    |
    |

REST API

    |
    |

RFC 9457 Errors

    |
    |

Backend Services
```

Cualquier nuevo servicio incorporado a NeuroInventory deberá utilizar el mismo estándar.

---

# Consecuencias

## Positivas

* Contrato de errores uniforme.
* Mejor experiencia de integración.
* Menor acoplamiento con frameworks.
* Facilita el manejo de errores en Angular.
* Facilita integración con servicios externos.
* Preparado para arquitecturas distribuidas.

---

## Negativas

* Requiere mantener una taxonomía de errores.
* Añade configuración inicial.
* Los desarrolladores deben seguir convenciones comunes.

---

# Evolución Futura

Esta decisión permite incorporar:

* Catálogo centralizado de errores.
* Documentación automática mediante OpenAPI.
* Correlación de errores mediante trazas distribuidas.
* Manejo uniforme en API Gateway.
* Exposición segura de errores a agentes IA.

---

# Referencias

* RFC 9457 - Problem Details for HTTP APIs.
* Spring Framework ProblemDetail.
* OpenAPI Specification.
