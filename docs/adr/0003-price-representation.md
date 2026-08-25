# ADR-0003: Representación del precio y dónde vive el formateo

## Estado

Aceptada — 2026-08-23. Revierte la representación del precio fijada en el diseño inicial y supera la
justificación de `price: String` de ADR-0002 §2.

## Contexto

`:core:database` cerró (PR #6) con `ProductEntity.price: String`. Al preguntarse cuál era el siguiente paso
—`:catalog:domain`— apareció una inconsistencia difícil de ignorar: `ProductCardUiModel.price` es un
`Double` y `FsPriceText(amount: Double)` formatea adentro del átomo. **Los dos extremos de la cadena, que
ya están construidos, usan `Double`; el del medio, que todavía no existe, iba a ser `Money` sobre
`BigDecimal`.**

Al buscar la justificación de esa pieza intermedia, el diseño original decía:

> `price` llega como número JSON. Un `BigDecimalSerializer` lee el **literal crudo** vía `JsonDecoder`
> (`decodeJsonElement().jsonPrimitive.content`) y construye el `BigDecimal` sin pasar por `Double`. `Money`
> nunca ve punto flotante.

Es una convención sensata —en el sistema del que viene—, pero nunca se justificó contra *esta* API ni
contra *este* alcance: se dio por buena de entrada. Este ADR la reevalúa contra tres fuentes: la API real,
el alcance real del proyecto, y lo que el `String` ya estaba costando.

### Evidencia 1 — la API

Sondeo en vivo de `GET https://fakestoreapi.com/products` (20 productos, esta sesión):

| Observación | Consecuencia |
|---|---|
| `price` llega como **número JSON** (`"price": 109.95`), no como string | Toda la premisa del "literal crudo" supone que el monto viaja como string. Acá no hay literal que preservar: el valor ya es un flotante en el cable antes de que el cliente lo toque. |
| **No existe campo de moneda.** Las claves son `id, title, price, description, category, image, rating` | Modelar `Money` obliga a *inventar* la moneda. Es el mismo criterio que este proyecto ya aplica en otras decisiones: si el dato no existe, inventarlo es deshonesto. |
| Decimales inconsistentes: **7 de 20 sin decimales** (`695`, `168`, `64`, `109`, `114`, `599`, `109`), uno con uno (`22.3`), 12 con dos | El "literal crudo" **no es presentable**. `22.3` debe mostrarse `$22.30`. Guardar el literal no preserva una representación útil: preserva una inconsistente. |
| Máximo `999.99` | `Double` tiene ~15 dígitos decimales significativos. Esto necesita 5. No hay problema de precisión que resolver. |

### Evidencia 2 — el proyecto no hace aritmética con el precio

Los cuatro requisitos son listar, ver detalle, favoritos y caché offline. Un grep sobre el plan completo
—las cuatro etapas, incluidas las que no se construyeron— no encuentra **ni una sola operación aritmética
sobre precio**: no hay carrito, ni total, ni impuesto, ni descuento, ni subtotal. La única aparición de
"carrito" en todo el diseño es una nota aclarando que la estrategia de resolución de conflictos elegida
sería inaceptable *para un carrito* — es decir, señalando que esta app no tiene uno.

Esto importa porque es exactamente la premisa que hace valioso a `BigDecimal`. El error de punto flotante
se **acumula** al operar; en un valor que solo se lee y se muestra, no hay dónde acumularse.

### Evidencia 3 — lo que el `String` ya estaba costando

- **`ORDER BY price` sería incorrecto.** En SQLite una columna `TEXT` ordena lexicográficamente.
  Comprobado con `sqlite3` sobre los precios reales:

  ```
  sqlite> create table t(price TEXT);
  sqlite> insert into t values('9.99'),('109'),('22.3'),('695');
  sqlite> select price from t order by price;
  109
  22.3
  695
  9.99      <-- el más barato, al final
  ```

- **El DTO no compilaría el camino simple.** `NetworkJson` no tiene `isLenient`, así que un
  `price: String` contra `"price": 109.95` lanza `JsonDecodingException`. El literal crudo obliga sí o sí
  al serializer custom que el diseño original describía — no era opcional, era el precio de entrada.

## Decisión

### 1. `price: Double` de punta a punta

```kotlin
ProductEntity.price: Double        // columna REAL
Product.price: Double              // :catalog:domain, cuando se construya
ProductCardUiModel.price: Double   // ya era así
```

Sin conversión, sin serializer custom, sin `TypeConverter`. El DTO deserializa `Double` nativamente, Room
guarda `REAL`, el átomo formatea.

### 2. `Money` no se escribe

Sale del lenguaje ubicuo de `:shared:kernel`. Es el mismo criterio ya aplicado a los convention plugins de
`build-logic`: una convención heredada no entra por inercia, se reevalúa contra el problema que tiene
delante. Acá la reevaluación concluye que no hay nada que escribir.

`BigDecimal` se gana su lugar donde hay aritmética monetaria real —comisiones, impuestos, subtotales,
totales— y donde el monto viaja como string y hay que preservar el literal. Ninguna de las dos condiciones
se cumple en este proyecto.

### 3. El formateo se queda dentro de `FsPriceText`

Sin cambios en el design system: se mantiene `String.format(Locale.US, "$%.2f", amount)` dentro del átomo.

Esto **no es inercia, es honrar una decisión ya documentada**. El design system la fija con su razón:

> Recibe el número, no el texto ya formateado. El formateo vive dentro del átomo para que exista un solo
> lugar donde cambiarlo a CLP.

Y la regla de pureza atómica del design system sigue intacta, porque `Double` no es un tipo de dominio:

> Un átomo no importa nada de domain ni de feature. `FsPriceText` recibe un `Double`, nunca un `Product`.

Beneficio lateral hacia la Etapa 2: el renderer SDUI resuelve `"price": "{product.price}"` apuntando al
**mismo átomo**, así que hereda el formato sin duplicarlo. Si el formateo viviera en un mapper de
`:catalog:ui`, el renderer tendría que replicarlo o llamar a un formatter compartido.

### 4. ¿El formateo es lógica de negocio?

Se mezclan dos cosas bajo la misma palabra, y solo una es dominio:

| | Qué es | Dónde va |
|---|---|---|
| Qué moneda es, y cuántos decimales tiene esa moneda | **Dominio.** Es propiedad del dinero, no de la pantalla: USD tiene 2, JPY tiene 0, KWD tiene 3. | `Money` en el kernel |
| Separadores del locale, dónde va el símbolo, abreviación | **Presentación.** Depende del dispositivo del usuario, que el dominio no debe conocer. | Capa de UI |

**Para FakeStore la primera fila está vacía**: la API no manda moneda, así que no hay semántica monetaria
que modelar. Queda solo la segunda fila, que es presentación — y por eso puede vivir en el átomo sin romper
el layering.

La respuesta corta: **acá no**, y la razón por la que no es un hecho verificable de la API, no una
preferencia de estilo.

## Consecuencias

- **Se pierde seguridad de tipos.** Nada impide pasar un `ratingRate: Double` donde va un `price: Double`;
  con `Money` el compilador lo atajaba. Es el costo real de esta decisión y se acepta a conciencia: a
  cambio desaparecen un value object, un serializer custom, un `TypeConverter` y una conversión en el ACL,
  para un valor que la app solo muestra.
- **Disparador explícito para revisitarlo**: si la API llegara a mandar moneda, o si el backend propio de
  la Etapa 2 agregara carrito, totales o impuestos, `Money` vuelve — y vuelve sobre `BigDecimal` o sobre
  minor units (`Long`), nunca sobre `Double`. Mientras eso no pase, este ADR es la respuesta.
- **`ORDER BY price` pasa a ser correcto**, y con él cualquier filtro o rango por precio que aparezca más
  adelante. Es una capacidad que el `String` no tenía.
- **`Formatters` en `:core:common` deja de tener contenido monetario.** Sigue reservado para fechas y
  otros formatos si aparecen, pero el dinero ya no pasa por ahí.
- **ADR-0002 §2 queda parcialmente superado** en su justificación de `price: String`. El resto de ADR-0002
  —versión de Room, forma de `ProductEntity`, DAOs, Hilt, y su Corrección sobre tests— se mantiene.
- **Es la segunda convención heredada que se rechaza tras evaluarla** (la primera fueron los convention
  plugins de `build-logic`, que se reescribieron en vez de copiarse). Refuerza que una referencia de estilo
  se mira y se decide, no se copia. Si aparece una tercera, este es el formato del registro.

## Notas de implementación

Cambio acotado a tres archivos, en una rama propia desde `main`:

```
core/database/.../entity/ProductEntity.kt        val price: String -> val price: Double
core/database/schemas/….FakeStoreDatabase/1.json regenerado por KSP: TEXT -> REAL
core/database/.../dao/ProductDaoTest.kt          price = "9.99" -> price = 9.99
```

**Se regenera v1; no se escribe migración.** Justificación verificada, no asumida: nada depende todavía de
`:core:database` — `app/build.gradle.kts` solo declara `:core:designsystem`, y ningún otro
`build.gradle.kts` lo referencia. La base **nunca fue instanciada en ningún dispositivo**, así que cambiar
el hash de identidad del schema no rompe a nadie. Vale dejarlo escrito porque cambiar un schema sin
migración es exactamente lo que se marca en una revisión: la defensa es que no hay usuarios, no que sea
inofensivo en general.

Fuera de este ADR:

- `:catalog:domain` — `Product.price: Double` queda decidido acá, pero el módulo se construye en su bloque.
- La alineación tabular del precio. El design system especifica `$ 22.30` con espacio y `$109.95` sin él, con
  `font-variant-numeric: tabular-nums` — una columna de dinero alineada a la derecha. El código produce
  `$22.30` sin relleno y `Type.kt` no setea `tnum`. Es una desviación del código contra su design system,
  no del precio contra su tipo; se trata cuando se retome `:core:designsystem`.