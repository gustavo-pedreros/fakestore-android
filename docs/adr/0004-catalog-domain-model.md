# ADR-0004: Modelo de dominio de `:catalog` y forma de sus casos de uso

## Estado

Aceptada — 2026-08-23. Corrige la línea de `:shared:kernel` del diseño inicial y amplía la lista de
casos de uso prevista. **Ampliada — 2026-08-23** por [ADR-0005](0005-catalog-data-layer.md): agrega un
quinto caso de uso, `ObserveLastSyncedAt`, con el mismo criterio de la decisión 3.

## Contexto

Los dos extremos de la cadena del catálogo están construidos y mergeados: `:core:database` (PR #6, más el
cambio de precio del PR #7) y `:core:designsystem` (PR #4 y #5). El del medio no existe.

Codegraph sobre el índice actual —80 archivos, 1.135 nodos, 1.609 aristas— confirma la forma del hueco:
**cero referencias a un tipo `Product` de dominio**. Lo que hay es `ProductEntity` en `:core:database` y
`ProductCardUiModel`/`ProductDetailUiModel` en `:core:designsystem`, y ninguno de los dos conoce al otro.
Los dos hablan primitivos —`ProductEntity.id: Int`, `ProductGrid(favoriteIds: Set<Int>)`— porque cada uno
respeta su propia regla de pureza: Room no debe conocer el dominio, y un átomo tampoco.

`:catalog:domain` es el módulo que introduce el lenguaje ubicuo entre ambos. Al planificarlo aparecieron
cuatro preguntas que el diseño inicial no tenía resueltas, o tenía resueltas antes de que existiera el código que
las iba a probar. Es el mismo patrón que [ADR-0003](0003-price-representation.md) encontró en la representación del precio.

## Decisión

### 1. Solo `ProductId` entra a `:shared:kernel`

El diseño inicial declaraba el lenguaje ubicuo del kernel como:

> `:shared:kernel` — Kotlin puro. Lenguaje ubicuo: ProductId, Category, Rating, AppError

La **regla 5** de dependencia dice:

> `:shared:kernel` es pequeño y **gobernado**: solo entra lo que dos o más contextos hablan de verdad.

Esa lista no pasa su propia regla. Al contarlos:

| Tipo | ¿Cuántos contextos lo hablan? | Dónde va |
|---|---|---|
| `ProductId` | **Dos.** `:favorites:domain` expone `ObserveFavoriteIds`, y [«sin foreign keys»](../ARCHITECTURE.md#sin-foreign-keys-entre-products-y-favorites) pide textualmente *"un `Set<ProductId>` para búsqueda O(1)"* en el cruce del ViewModel. | `:shared:kernel` |
| `Category` | **Uno.** Favoritos no filtra ni muestra categorías. | `:catalog:domain` |
| `Rating` | **Uno.** | `:catalog:domain` |

`ProductId` es el único que cruza el límite entre contextos, y es justamente el tipo cuyo cruce la regla 4
autoriza: *"cruce entre contextos: solo a nivel `domain`"*.

**Disparador para revisitarlo**: si `:favorites:ui` mostrara la categoría de cada favorito, `Category`
pasaría a ser hablada por dos contextos y subiría al kernel. Mientras no pase, baja.

### 2. `Category` es un `value class` abierto, no un `enum`

```kotlin
@JvmInline
value class Category(val value: String)
```

La alternativa evidente es un `enum class Category(val wireValue: String)` con
`fromWire(raw: String?): Category?`. Se descarta, y la razón es el modo de falla.

Sondeo en vivo de `GET https://fakestoreapi.com/products/categories`:

```json
["electronics","jewelery","men's clothing","women's clothing"]
```

Cuatro, y hoy son estables. Pero **el set no es contractual**: FakeStore no publica ninguna garantía de que
sean esos cuatro para siempre. Con `enum` + `fromWire`, una quinta categoría devuelve `null`, el producto
**pierde su categoría**, y desaparece del filtro sin que nadie se entere — un producto invisible por un
campo que llegó bien. Con `value class`, la categoría nueva fluye hasta el chip y la lista de filtros crece
sola (ver decisión 4).

Es el mismo criterio que [ADR-0003](0003-price-representation.md) aplicó al campo de moneda: no modelar como garantía lo que la API no
garantiza.

**El dominio no normaliza ni transforma el string.** `FsChip` ya hace `label.uppercase()`
(`FsChip.kt:68`), así que las mayúsculas del diseño (`TODOS / JEWELERY / ELECTRONICS / MENS`) son
presentación. El dominio guarda `men's clothing` tal como llega, apóstrofe incluido — Room bindea el
parámetro, no lo interpola, así que no hay nada que escapar.

**Se paga**: no hay `when` exhaustivo, y nada garantiza en compilación que una `Category` sea una de las
cuatro conocidas. Si en la Etapa 2 el backend propio publica el set como contrato, o si alguna categoría
gana comportamiento propio, ahí el `enum` se justifica.

### 3. La forma del caso de uso la decide su contenido

Criterio: **`fun interface` si es un proxy; clase concreta si tiene lógica real adentro.**

| Caso de uso | Contenido | Forma |
|---|---|---|
| `ObserveCatalog` | delega en el repositorio — filtro y orden los resuelve SQL | `fun interface` |
| `ObserveProductDetail` | delega | `fun interface` |
| `RefreshCatalog` | delega | `fun interface` |
| `ObserveCategories` | **distinct + orden** sobre el catálogo cacheado | **clase concreta** |

> **Nota — 2026-08-23.** [ADR-0005](0005-catalog-data-layer.md) agrega un quinto caso de uso con este mismo
> criterio: `ObserveLastSyncedAt` es un proxy puro (`fun interface`) sobre
> `CatalogRepository.observeLastSyncedAt()`. La tabla y el conteo de abajo —"tres `fun interface`"— quedan
> como se escribieron en su momento; el estado actual es **cuatro** `fun interface` más `ObserveCategories`.

Los tres `fun interface` se bindean con un lambda en el módulo Hilt de `:catalog:data` — **no acá**:
`:catalog:domain` es Kotlin/JVM puro y no conoce Hilt. El binding y el dominio quedan en módulos Gradle
distintos por la regla de que una capa es un módulo:

```kotlin
@Provides fun provideObserveCatalog(r: CatalogRepository) = ObserveCatalog { c -> r.observeAll(c) }
```

**Gano**: cero clases proxy, vocabulario de dominio nombrado, y en los tests de ViewModel el fake
es un lambda —`ObserveCatalog { flowOf(products) }`— en vez de una clase falsa por caso de uso.

**Pago**: tres interfaces que hoy no tienen cuerpo. Se les puede llamar ceremonia; la respuesta es que
cuestan una línea cada una y compran el seam que hace testeable al ViewModel sin dobles de clase.

**Sin `javax.inject` en el dominio.** `ObserveCategories` es una clase con constructor plano, provista
desde el mismo módulo Hilt. Así `:catalog:domain` no gana ni una anotación y todo el cableado vive en un
solo lugar.

### 4. El filtro por categoría entra ahora, y se deriva de Room

`CategoryFilterRow(categories: List<String>, selected: String?, onSelect: (String?) -> Unit)` ya existe
(`molecule/CategoryFilterRow.kt:29`) y el diseño de la lista de productos dibuja los chips. El diseño está
congelado; la firma del dominio se escribe una sola vez.

`ObserveCategories` **no agrega un método al repositorio, ni una query al DAO, ni una llamada de red**:
deriva las categorías del mismo `observeAll(null)` que alimenta la grilla.

`GET /products/categories` existe —el sondeo de arriba salió de ahí— y **se descarta a propósito**. [«Room como fuente única de verdad»](../ARCHITECTURE.md#room-como-fuente-única-de-verdad) es
categórica: *"la UI solo lee de Room"*. Pintar los chips desde una llamada de red rompería esa regla justo
en el requisito 4: sin conexión, la grilla tendría productos y el filtro estaría vacío.

**Gano**: las categorías del filtro no pueden desincronizarse de los productos mostrados, porque son los
mismos datos. Funciona offline por construcción. Y le da al módulo su única lógica testeable de verdad.

**Pago**: `distinct` + `sorted` en memoria en vez de `SELECT DISTINCT`. Con 20 productos es irrelevante;
si el catálogo creciera un orden de magnitud, pasa a ser una query y el caso de uso vuelve a ser un proxy.

## Consecuencias

- **La línea del kernel del diseño inicial queda corregida** en la línea del kernel: `Category` y `Rating` bajan a
  `:catalog:domain`. El resto de la línea —`ProductId`, `AppError`, y las exclusiones de `ErrorCode`
  ([ADR-0001](0001-networking-module.md)) y `Money` ([ADR-0003](0003-price-representation.md))— se mantiene.
- **La lista de casos de uso gana un cuarto.** La lista decía `ObserveCatalog`, `RefreshCatalog`,
  `ObserveProductDetail`; `ObserveCategories` se suma con el filtro. (Un quinto, `ObserveLastSyncedAt`, se
  suma con [ADR-0005](0005-catalog-data-layer.md).)
- **El kernel queda en dos tipos** (`AppError`, `ProductId`) al cierre de este bloque. Que sea pequeño es
  la intención de la regla 5, no un síntoma de que falte algo.
- **`:catalog:domain` tendrá un solo test**, y es correcto: los tres `fun interface` no tienen cuerpo que
  probar y los value objects no tienen invariantes. El peso de tests de `:catalog` cae en `:catalog:data`
  (mappers ACL contra MockWebServer + Room) y en `:catalog:ui`.
- **Es la tercera convención heredada que se rechaza tras evaluarla**, después de los convention plugins
  de `build-logic` y de `Money` (ADR-0003). Acá el rechazo es parcial: se conserva la forma de caso de uso
  (`fun interface` + binding en DI) y se descarta la de `Category` (`enum` + `fromWire`).

## Notas de implementación

Sin invariantes que lancen. Ni `ProductId`, ni `Category`, ni `Rating` validan nada:

- `ProductId` envuelve la PK que manda la API. No hay invariante que imponer que no sea inventada.
- `Rating` **no valida el rango 0..5**. `FsRatingStars` ya hace `floor(rate).toInt().coerceIn(0, StarCount)`
  (`FsRatingStars.kt:50`): el único lugar donde un valor fuera de rango importaría ya lo acota. Un
  `require` acá tumbaría el refresh completo por un producto mal formado — peor que mostrarlo con cinco
  estrellas.

`Product` **no lleva `isFavorite`**. [«Sin foreign keys»](../ARCHITECTURE.md#sin-foreign-keys-entre-products-y-favorites) es explícita: sin foreign keys, el cruce ocurre en el ViewModel
con `combine`. Meter la bandera en el modelo acoplaría los dos contextos justo donde esa decisión los
separa.

El orden del catálogo es **parte del contrato del repositorio**, no un detalle de implementación: un
`SELECT` sin `ORDER BY` devuelve orden indefinido y la grilla parpadearía en cada refresh. Se elige `id`
ascendente porque reproduce el orden que la propia API sirve, sin inventar un criterio que el diseño no
pidió. Ordenar por precio —hoy posible gracias a la columna `REAL` de [ADR-0003](0003-price-representation.md)— espera a que exista un
control de orden en la UI.

