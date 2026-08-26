# ADR-0005: La capa de datos del catálogo — dos datasources y una sola escritura

## Estado

Aceptada — 2026-08-23. Amplía [ADR-0004](0004-catalog-domain-model.md) (`CatalogRepository` gana un método y aparece un quinto caso de
uso), reabre con una segunda entity el schema que fijó [ADR-0002](0002-database-module.md), y supera la fila «Fechas» d[el stack](../ARCHITECTURE.md#3-stack) en
`ARCHITECTURE.md`.

## Contexto

`:catalog:domain` (PR #8) declaró `CatalogRepository` y no lo implementa nadie. Los dos módulos técnicos
que tienen que satisfacerlo están construidos y mergeados —`:core:database` (PR #6 y #7) y `:core:network`
(PR #3)— y ninguno de los dos sabe del otro: `ProductDao` habla `ProductEntity`, `executeCall` habla
`Either<AppError, T>`, y entre medio no hay nada.

Este bloque es donde [«Room como fuente única de verdad»](../ARCHITECTURE.md#room-como-fuente-única-de-verdad) deja de ser una frase del plan:

> La UI **solo lee de Room**, vía `Flow`. La red escribe en Room; nunca alimenta la UI directamente.

Escribir eso en un README es gratis. Las cuatro decisiones que siguen son las que lo convierten en una
propiedad del código, verificable sin confiar en nadie.

## Decisión

### 1. Cada datasource habla su tipo técnico; el repositorio es el único que conoce las tres formas

```kotlin
internal interface CatalogRemoteDataSource {
    suspend fun fetchCatalog(): Either<AppError, List<ProductDto>>
}

internal interface CatalogLocalDataSource {
    fun observeAll(category: String?): Flow<List<ProductEntity>>
    fun observeById(id: Int): Flow<ProductEntity?>
    suspend fun syncAll(rows: List<ProductEntity>)
    fun observeLastSyncedAt(): Flow<Instant?>
    suspend fun writeLastSyncedAt(at: Instant)
}
```

El remoto no sabe que existe Room; el local no sabe que existe la red. `CatalogRepositoryImpl` es la única
clase del repositorio que ve `ProductDto`, `ProductEntity` y `Product` a la vez — que es, literalmente, la
definición del asiento de la ACL.

La alternativa evaluada era que los datasources hablaran dominio: el remoto devolvería `List<Product>`, el
local persistiría `Product`, y `CatalogRepositoryImpl` quedaría en cuatro líneas de cableado. Se descartó
por lo que arrastra.

**Gano** una consecuencia que ninguna otra forma produce: **no existe un mapper `ProductDto → Product` en
toda la codebase**. Solo hay `Dto → Entity` (camino de escritura) y `Entity → Product` (camino de
lectura). Que ese tercer mapper no se pueda escribir sin que sobre es la prueba mecánica de que Room es el
Single Source of Truth. Con los datasources hablando dominio, el `Dto → Product` vuelve a existir, y con él
la posibilidad de que alguien —en seis meses, apurado— devuelva el resultado de la red directo a la UI sin
pasar por Room. Una regla se defiende sola cuando el atajo no está disponible.

**Pago**: `CatalogRepositoryImpl` tiene dos mapeos adentro. No es un cable; es el borde.

Hay un segundo motivo, específico de este repo. Los datasources son **interfaces**, y eso hace que
`CatalogRepositoryImplTest` sea un test Jupiter puro contra dos fakes escritos a mano. Sin ese seam habría
que levantar Robolectric **y** MockWebServer para probar el repositorio — un test de integración disfrazado
de unitario. Dado lo que costó Robolectric en `:core:database` (ver sus desviaciones: driver bundled
inservible en un `android.library`, `UnsatisfiedLinkError`, JUnit4 puenteado por Vintage), mantener
Robolectric acotado al único módulo donde de verdad se ejecuta SQL no es purismo, es presupuesto.

**Cuándo elegiría otra cosa**: en un contexto sin persistencia local, los datasources sobran y el
repositorio habla con la API directo y la capa de datasources es puro andamiaje. La abstracción se
justifica cuando hay dos fuentes que arbitrar, no antes.

### 2. El refresh escribe con upsert + poda por `id`, dentro de una `@Transaction`

Hoy `ProductDao` solo sabe hacer `@Upsert`. Eso alcanza para el camino feliz y falla en el interesante: si
el servidor deja de publicar un producto, la fila queda de fantasma en la caché para siempre, visible en la
grilla, sin forma de sacarla salvo borrar los datos de la app.

```kotlin
@Upsert
suspend fun upsertAll(products: List<ProductEntity>)

@Query("DELETE FROM products WHERE id NOT IN (:ids)")
suspend fun deleteMissing(ids: List<Int>)

@Transaction
suspend fun syncAll(products: List<ProductEntity>) {
    upsertAll(products)
    deleteMissing(products.map(ProductEntity::id))
}
```

**Gano** tres propiedades, y las tres son de offline-first:

1. **Las filas que el servidor dejó de publicar se van.**
2. **La grilla no parpadea.** El `InvalidationTracker` de Room notifica al commit de la transacción, no por
   operación: los observadores ven una sola emisión con el estado final, nunca el intermedio entre el
   upsert y la poda.
3. **Un refresh fallido jamás destruye la caché.** Si la escritura muere a medias, la transacción hace
   rollback y Room queda como estaba. Es la invariante que [la tabla de estados de arranque](../ARCHITECTURE.md#hay-caché-decide-si-un-error-es-bloqueante) exige —*"un fallo de refresh nunca debe
   tapar datos que el usuario ya tenía"*— y acá se cumple por construcción, no por un `try/catch`.

**Pago**: una query más que `deleteAll` + `upsertAll`, y un parámetro `:ids` que crece con el catálogo.

**Cuándo elegiría otra cosa**: `DELETE FROM products` seguido del upsert es más corto y expresa mejor
*"esto es un snapshot completo"*, que es lo que `GET /products` devuelve. Se prefirió la poda porque no
reescribe las ~20 filas que no cambiaron, y porque sobrevive el día que ese endpoint se pagine — momento en
que borrar todo pasaría de elegante a incorrecto.

### 3. `lastSyncedAt` entra en este bloque, y no trae ninguna dependencia

La tabla de estados de arranque pide que el `FsStatusBanner` muestre la antigüedad de los datos cuando el refresh falla con caché
presente. Quien **escribe** ese timestamp es la capa de datos; quien lo **lee** es `:catalog:ui`. La regla
que este repo aplicó cinco veces —*no se construye sin un consumidor real delante*— empujaba a diferirlo.
Se decidió al revés, porque el precio que la regla estaba estimando ya no existe.

**`kotlinx-datetime` no entra.** `kotlin.time.Instant` y `kotlin.time.Clock` están en el stdlib con
`@SinceKotlin("2.3")` + `@WasExperimental(ExperimentalTime::class)` — verificado con `javap` sobre
`kotlin-stdlib-2.3.0.jar`, no de memoria. Estables, sin `@OptIn`, y 2.3 es justo la versión del proyecto.
[El stack](../ARCHITECTURE.md#3-stack) listaba `kotlinx-datetime` para fechas, y se lo contaba como parte del coste de diferir `sync_metadata`; ese
coste dejó de existir hace una versión de Kotlin.

**No hay migración que escribir.** `:app` depende solo de `:core:designsystem`: `DatabaseModule` nunca se
instancia en runtime, así que `fakestore.db` no existe en ningún dispositivo. La entity nueva regenera
`schemas/…/1.json` en el sitio, sin `Migration` y sin bump de versión — mismo precedente y mismo argumento
que [ADR-0003](0003-price-representation.md) usó para pasar `price` de `TEXT` a `REAL`.

**No hay `TypeConverter`.** La columna guarda `Long` (epoch millis UTC), no `Instant`. `:core:database`
conserva así la propiedad que tiene desde el PR #6 —*todos los campos son tipos nativos de Room*— y el
`Long ↔ Instant` ocurre en el mapper de `:catalog:data`, que es donde ya vive la traducción entre mundos.

```kotlin
@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val scope: String,   // "catalog"
    val lastSyncedAt: Long,
)
```

**Gano**: la capa de datos queda con la historia offline completa. `:catalog:ui` va a consumir un `Flow`
que ya existe en vez de pedir que se lo construyan, y el bloque siguiente no tiene que reabrir tres
módulos para pintar una banda.

**Pago**: dos módulos marcados `[x]` se reabren (`:core:database` y `:catalog:domain`) y este ADR enmienda
al anterior. Son ~90 líneas de producción y una corrección documental, no una reescritura.

**Cuándo elegiría otra cosa**: si el timestamp hubiera traído una dependencia nueva, un `TypeConverter` y
una migración —el precio que la estimación original suponía— diferirlo era lo correcto. La decisión no
cambió de criterio; cambió el precio.

### 4. `DispatcherProvider` sigue sin escribirse

Quedó anotado que se construye *"cuando `:catalog:data` lo
necesite de verdad"*, con `io` y sin `default`. Toca revisitarlo con el código concreto delante, y el
hallazgo original se sostiene: las funciones `suspend` de un DAO corren en el executor propio de Room, y
`executeCall` ya hace `withContext(Dispatchers.IO)`. Lo único que queda en el hilo del colector es mapear
~20 `ProductEntity` por emisión.

**Gano**: `:core:common` no crece, y no hace falta un `TestDispatcherProvider` — que hoy no tendría dónde
vivir, porque `:core:testing` sigue sin existir.

**Pago**: el repositorio asume que ocupar el hilo del colector con el mapeo de lectura es barato. Con 20
productos lo es; es una suposición sobre la escala, no sobre la corrección.

**Disparador**: cuando el mapeo del camino de lectura deje de ser trivial, entra un `.flowOn(...)`, y un
`flowOn` con un dispatcher fijo es justamente lo que un proveedor inyectable evita. Ahí se escribe.

## Consecuencias

- **No hay mapper `ProductDto → Product`, y no debe haberlo.** Si alguna vez aparece uno, es la señal de
  que alguien saltó Room. Es la regla de [«Room como fuente única de verdad»](../ARCHITECTURE.md#room-como-fuente-única-de-verdad) expresada como ausencia de un archivo — más barata de
  auditar que cualquier test.
- **`:core:database` pasa de una entity a dos** y su `identityHash` cambia (hoy `2777658269ff955db…`). El
  `1.json` regenerado se commitea, igual que la primera vez: es el baseline de migraciones, no output
  desechable.
- **`SyncMetadataDao` es el primer DAO que van a compartir dos contextos.** Favoritos lo va a querer en la
  Etapa 2 para su `syncState`. «Room como fuente única de verdad» promete *"cada `*:data` consume únicamente su propio DAO"*; con la
  tabla `scope`-eada la garantía se degrada a *"cada contexto consume sus propias filas"*. Se acepta y se
  dice en voz alta. La alternativa que la preserva —una tabla `catalog_sync` dedicada, y otra cuando
  favoritos la pida— cambia una verruga conocida por N tablas de una fila.
- **La escritura de productos y la del timestamp no son atómicas entre sí.** Son dos DAOs, y una
  `@Transaction` de Room no abarca los dos desde adentro de uno. Las salidas son peores:
  `FakeStoreDatabase.withTransaction { }` obliga a `:catalog:data` a inyectarse la base entera —rompiendo
  el *"solo su propio DAO"* de forma más fuerte que la tabla compartida—, y meter el upsert de metadata en
  `ProductDao` es un DAO escribiendo dos tablas por conveniencia. Se escribe **productos primero,
  timestamp después**: si el proceso muere entremedio, la banda dice que los datos son *más viejos* de lo
  que son, nunca más nuevos. El fallo es conservador. Cambiaría si el timestamp decidiera algo de
  corrección —expirar la caché, disparar un sync— en vez de solo pintar un texto.
- **[ADR-0004](0004-catalog-domain-model.md) queda ampliada**: `CatalogRepository` gana `observeLastSyncedAt(): Flow<Instant?>` y los casos
  de uso pasan de cuatro a cinco. `ObserveLastSyncedAt` es un proxy puro, así que es `fun interface` — la
  regla de [ADR-0004, Decisión 3](0004-catalog-domain-model.md#3-la-forma-del-caso-de-uso-la-decide-su-contenido) se aplica sin excepción.
- **[El stack](../ARCHITECTURE.md#3-stack) queda superado** en la fila «Fechas»: `kotlinx-datetime` sigue siendo la respuesta
  correcta para calendario, zonas horarias y formateo (probablemente en `:catalog:ui`), pero no para
  «un instante y un reloj», que hoy es stdlib.
- **`:catalog:data` no aplica `fakestore.android.room`.** Usa `ProductDao` y `ProductEntity` como tipos
  Kotlin comunes, sin una sola anotación de Room en su código. Si algún día necesitara el plugin, es la
  señal de que un detalle de persistencia se filtró al módulo equivocado.
- **Todo `:catalog:data` es `internal`.** El módulo no exporta un solo símbolo público: su única superficie
  es el grafo de Hilt. Es lo que [la regla 2 de dependencia](../ARCHITECTURE.md#reglas-de-dependencia) quiere decir con *"`*:data` implementa las interfaces
  que declara su `*:domain`"*.

## Notas de implementación

**`syncAll(emptyList())` vacía la tabla, y es deliberado.** SQLite acepta `NOT IN ()` —a diferencia de la
mayoría de los motores SQL— y lo evalúa como verdadero para todas las filas. [La tabla de estados de arranque](../ARCHITECTURE.md#hay-caché-decide-si-un-error-es-bloqueante) modela explícitamente el
caso *"OK, 0 items → `Empty`"*, así que un catálogo vacío del servidor es una respuesta legítima y no un
error a ignorar. Lleva su propio test para que nadie lo «arregle» por accidente.

**Contingencia sobre el método `@Transaction`**: `syncAll` es un método con cuerpo sobre una `interface`,
lo que depende de que el compilador emita default methods reales (`-jvm-default`). Si Room lo rechaza,
`ProductDao` pasa a `abstract class` — un cambio de una línea que no toca ni `DatabaseModule` ni
`ProductDaoTest`.

**`distinctUntilChanged()` en el datasource local no es decorativo.** El `InvalidationTracker` de Room
re-emite ante *cualquier* escritura sobre `products`, aunque el resultado de la query sea idéntico. Sin
él, un refresh que no cambia nada recompone la grilla entera. Con 20 data classes la comparación es
gratis.

**Las llamadas `suspend` dentro de `Either.map` compilan** porque `map` es `inline`: el lambda se inlinea
en el cuerpo de `refresh`, que ya es `suspend`. No hace falta un combinador nuevo en `:core:common`.

**`GET /products/{id}` no se declara en `CatalogApi`.** El dominio no tiene ningún método que lo usaría —el
detalle sale de Room— y no sería gratis: un sondeo en vivo de `/products/999` devuelve **HTTP 200
con body vacío**, justo el caso para el que existen `EmptyBodyAwareConverterFactory` y `AppError.EmptyBody`
(corrección de [ADR-0001](0001-networking-module.md)). El reintento del detalle sin caché llama `RefreshCatalog`, que trae el catálogo
completo.

**Los campos de `ProductDto` son no-nulos y sin defaults.** Un payload mal formado lanza
`SerializationException`, `executeCall` la atrapa y la convierte en `AppError.Unknown`; el refresh falla,
la caché queda intacta y la banda muestra los datos viejos. Esa degradación cae sola del diseño de la
decisión 2 — no hay que programarla.

