# Architecture Decision Records (ADR)

## Propósito

Este directorio contiene las decisiones arquitectónicas relevantes tomadas durante la evolución de NeuroInventory.

Cada ADR documenta:

* El contexto que originó una decisión.
* Las alternativas consideradas.
* La decisión adoptada.
* Las consecuencias positivas y negativas.

El objetivo es mantener un registro histórico que permita comprender por qué el sistema fue diseñado de determinada manera.

---

# Índice de Decisiones

| ADR      | Título                                       | Estado   |
| -------- | -------------------------------------------- | -------- |
| ADR-0001 | Adoptar Clean Architecture                   | Aceptada |
| ADR-0002 | Utilizar PostgreSQL + pgvector               | Aceptada |
| ADR-0003 | Separar AI Service del dominio principal     | Aceptada |
| ADR-0004 | Utilizar Keycloak para identidad y seguridad | Aceptada |
| ADR-0005 | Adoptar RFC 9457 para errores HTTP           | Aceptada |
| ADR-0006 | Utilizar modelos LLM locales mediante Ollama | Aceptada |
| ADR-0007 | Adoptar MCP para integración con agentes IA  | Aceptada |
---

# Estados posibles

Las decisiones pueden tener los siguientes estados:

| Estado      | Descripción                       |
| ----------- | --------------------------------- |
| Propuesta   | Decisión en evaluación            |
| Aceptada    | Decisión adoptada                 |
| Deprecada   | Ya no aplica                      |
| Reemplazada | Sustituida por una nueva decisión |

---

# Formato

Cada ADR sigue la estructura:

```markdown
# ADR-NNNN: Título

## Estado

Aceptada

## Contexto

¿Por qué necesitamos tomar esta decisión?

## Decisión

¿Qué solución elegimos?

## Alternativas consideradas

¿Qué otras opciones evaluamos?

## Consecuencias

¿Qué beneficios y costos tiene esta decisión?
```
