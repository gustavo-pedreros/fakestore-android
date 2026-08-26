# Documentación de FakeStore Android

> Esta carpeta está en español. El [README raíz](../README.md) del repo está en inglés: es el punto de
> entrada al proyecto, con capturas, requisitos y cómo compilar. Acá vive el razonamiento.

## Por dónde empezar

**[ARCHITECTURE.md](ARCHITECTURE.md)** — el mapa completo: las dos ideas que ordenan el módulo graph, las
reglas de dependencia, el stack, las decisiones que atraviesan la app, lo que quedó diseñado sin construir,
y una tabla de doce decisiones con su costo. Si solo vas a leer un archivo, es este.

Después, los ADR, en orden o por tema.

## Los ADR

Un ADR (*Architecture Decision Record*) registra **una decisión, en su momento, con la evidencia que la
sostuvo**. Todos siguen la misma plantilla: Estado · Contexto · Decisión · Consecuencias · Notas de
implementación.

| # | Decide |
|---|---|
| [0001](adr/0001-networking-module.md) | Alcance mínimo de `:core:network` |
| [0002](adr/0002-database-module.md) | Room: versión, entities, y la estrategia de tests |
| [0003](adr/0003-price-representation.md) | El precio es un `Double`, no un `Money` |
| [0004](adr/0004-catalog-domain-model.md) | Modelo de dominio del catálogo |
| [0005](adr/0005-catalog-data-layer.md) | Capa de datos del catálogo |
| [0006](adr/0006-catalog-presentation.md) | Capa de presentación y Navigation 3 |
| [0007](adr/0007-favorites-context.md) | Contexto de favoritos |

## Cómo se enmienda un ADR acá

Una decisión que resultó equivocada **no se borra ni se reescribe**: se marca como superada y la corrección
se agrega como sección propia, con la fecha. Un ADR registra por qué se decidió algo *en su momento*;
borrar el error borra el aprendizaje.

[ADR-0002](adr/0002-database-module.md) es el ejemplo más completo: su Decisión 5 eligió un driver de tests que
falló dos veces seguidas, y esa sección sigue ahí, marcada, seguida de la causa raíz y de lo que realmente
se construyó.
