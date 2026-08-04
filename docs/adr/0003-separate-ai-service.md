# ADR-0003: Separar AI Service del dominio principal

## Estado

Aceptada

---

## Contexto

NeuroInventory evoluciona desde una aplicación empresarial tradicional de inventario hacia una plataforma con capacidades de inteligencia artificial.

El sistema requiere funcionalidades como:

* Generación de embeddings.
* Búsqueda semántica.
* Retrieval-Augmented Generation (RAG).
* Integración con modelos de lenguaje.
* Procesamiento de documentos.
* Preparación para agentes de IA mediante MCP.

Estas capacidades tienen características técnicas diferentes a las operaciones tradicionales del inventario:

* Utilizan modelos de machine learning.
* Requieren librerías especializadas de Python.
* Tienen ciclos de evolución distintos.
* Pueden cambiar de proveedor o modelo independientemente.

Se debe decidir si estas capacidades deben formar parte del backend principal o implementarse como un servicio independiente.

---

## Decisión

Se crea un servicio independiente denominado **AI Service** responsable exclusivamente de las capacidades relacionadas con inteligencia artificial.

La arquitectura resultante será:

```text id="iq1klw"
                 Frontend

                    |
                    |

           Inventory Service
                    |
                    |

              AI Service

                    |
        ┌───────────┴───────────┐
        |                       |

 PostgreSQL + pgvector       LLM

                            Ollama
```

El **Inventory Service** mantiene la responsabilidad del dominio empresarial.

El **AI Service** mantiene la responsabilidad de:

* Procesamiento inteligente.
* Generación y consulta de embeddings.
* Recuperación de contexto.
* Comunicación con modelos LLM.

---

# Responsabilidades

## Inventory Service

Responsable de:

* Productos.
* Categorías.
* Stock.
* Movimientos.
* Reglas de negocio.
* Seguridad.
* APIs empresariales.

No contiene lógica específica de inteligencia artificial.

---

## AI Service

Responsable de:

* Modelos de embeddings.
* Procesamiento de documentos.
* Búsqueda semántica.
* Pipeline RAG.
* Comunicación con modelos LLM.
* Preparación de herramientas para agentes IA.

---

# Alternativas consideradas

## Integrar IA dentro del Inventory Service

Ejemplo:

```text id="5xtc0s"
Spring Boot

    |

    ├── Inventory Logic

    └── AI Logic
```

### Ventajas

* Menor cantidad inicial de servicios.
* Comunicación más sencilla.
* Menor infraestructura.

### Desventajas

* Mezcla responsabilidades diferentes.
* Incrementa el tamaño del backend.
* Acopla el dominio a librerías de IA.
* Dificulta cambiar tecnologías.
* Complica pruebas y despliegues independientes.

---

## Crear una librería interna de IA

Ejemplo:

```text id="7c8jv6"
inventory-service

        +

ai-library
```

### Ventajas

* Reutilización de código.
* Menos servicios desplegables.

### Desventajas

* Mantiene el acoplamiento dentro del backend.
* Los ciclos de actualización siguen unidos.
* La infraestructura sigue mezclada.

---

## Usar un proveedor externo de IA directamente desde el backend

Ejemplo:

```text id="u2y0cn"
Inventory Service

        |
        |

 OpenAI / Cloud Provider
```

### Ventajas

* Implementación rápida.
* Sin gestión de modelos.

### Desventajas

* Dependencia externa.
* Posibles problemas de privacidad.
* Menor control sobre modelos.
* Costos variables.

---

# Consecuencias

## Positivas

* Separación clara de responsabilidades.
* El dominio de inventario permanece limpio.
* Permite evolucionar la IA independientemente.
* Facilita cambiar modelos o proveedores.
* Permite escalar la carga de IA por separado.
* Facilita experimentar con nuevas tecnologías.

---

## Negativas

* Mayor complejidad operacional.
* Requiere comunicación entre servicios.
* Necesita definir contratos claros entre APIs.
* Introduce consideraciones de red y disponibilidad.

---

# Comunicación entre Servicios

La comunicación inicial se realizará mediante APIs HTTP.

Ejemplo:

```text id="y2sk1k"
Inventory Service

POST /ai/search

        |
        v

AI Service

POST /semantic-search
```

Las interfaces deben mantenerse independientes de la implementación interna del servicio de IA.

---

# Consideraciones de Seguridad

El AI Service no debe exponer directamente capacidades sensibles.

Las solicitudes deben:

* Ser autenticadas.
* Validar permisos.
* Controlar el acceso a información del dominio.

El servicio principal mantiene el control sobre las operaciones del negocio.

---

# Evolución Futura

Esta separación permite incorporar nuevas capacidades sin modificar el backend principal:

* Agentes IA mediante MCP.
* Nuevos modelos LLM.
* Procesamiento multimodal.
* Análisis predictivo.
* Automatizaciones inteligentes.
* Pipelines avanzados de conocimiento.

La arquitectura permite que la inteligencia artificial evolucione como una capacidad independiente dentro de la plataforma.

---

# Referencias

* Domain-Driven Design.
* Clean Architecture.
* Microservices Architecture Patterns.
* Retrieval-Augmented Generation (RAG).
