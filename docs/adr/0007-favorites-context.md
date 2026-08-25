# ADR-0007: El contexto de favoritos — la tabla nace mirando al sync, y el toggle no le cree a la UI

## Estado

Aceptada — 2026-08-24. Cierra el contexto de favoritos. Con este bloque **los cuatro
requisitos funcionales quedan cumplidos**. Resuelve la advertencia que el diseño inicial dejó abierta sobre
`AppErrorStrings` (§2), salda la deuda consciente que declaró ADR-0006 §4, y le da a `:core:database` su
**primera migración**.

## Contexto

El requisito 3 —*favoritos persistentes entre reinicios*— es el único funcional que falta, y hoy el corazón
**se dibuja y no responde** en las dos pantallas. Eso no es un olvido: fue la decisión §4 de ADR-0006, tomada
para no simular con `remember { mutableStateOf }` justo el requisito que pide persistencia. La consecuencia
es que la demo actual arrastra tres controles muertos.

Lo interesante de este bloque no es el requisito, que es modesto. Es que **es el primer contexto que cruza a
otro**. `:catalog` se construyó entero sin depender de nadie más; la regla 4 de §1.3 —«cruce entre contextos
solo a nivel `domain`»— estaba escrita pero nunca ejercida. Acá se ejerce en las dos direcciones:

| Arista | Por qué | Estado en §1.3 |
|---|---|---|
| `:catalog:ui → :favorites:domain` | la grilla necesita saber qué está marcado | ya documentada |
| `:favorites:ui → :catalog:domain` | la pantalla de favoritos muestra productos | su espejo, §1 de este ADR |

Los seams ya están puestos, y eso decide buena parte del diseño:

| Seam existente | Dónde | Qué falta |
|---|---|---|
| `CatalogUiState.favoriteIds: Set<Int> = emptySet()` | `catalog/ui/catalog/CatalogUiState.kt` | que alguien lo llene |
| `ProductGrid(favoriteIds, onFavoriteClick = { _, _ -> })` | `catalog/ui/catalog/CatalogScreen.kt` | el handler |
| `ProductDetailUiState.isFavorite: Boolean = false` | `catalog/ui/detail/ProductDetailUiState.kt` | derivarlo |
| `ProductDetailHeader(onFavoriteClick = { })` | `catalog/ui/detail/ProductDetailScreen.kt` | el handler |
| `FsListTopBar(onFavoritesClick)` → `{ }` | `app/navigation/FakeStoreNavHost.kt` | la pantalla a la que ir |
| `FsFavoriteButton(checked, saving)`, `ProductGrid(savingFavoriteIds)` | `:core:designsystem` | nada — ya existen |

**No entra ninguna librería nueva.** Room, Hilt, coroutines, Navigation 3, serialization y Turbine ya están
en el catálogo de versiones y ya los usa algún módulo. Es la mejor señal que dejó el bloque anterior.

Lo que sí cambia de verdad es la base de datos: `schemas/…/1.json` está commiteado y es el baseline contra
el que Room valida. Agregar la tabla `favorites` obliga a v2, y ese es el riesgo real del bloque.

## Decisión

### 1. La pantalla de favoritos vive en `:favorites:ui`, que consume `:catalog:domain`

Es el espejo exacto de la arista que la regla 4 ya documenta. El catálogo necesita saber qué es favorito;
favoritos necesita saber cómo se ve un producto. Las dos son **Customer/Supplier a nivel `domain`**, que es
lo que la regla permite y —esto importa— lo único que permite.

La alternativa real no era «no hacer el módulo», era **meter la pantalla en `:catalog:ui`** y argumentar que
favoritos es un catálogo filtrado. Es un argumento decente: la pantalla muestra productos, reusa el mapper,
reusa los textos, y `:favorites` se queda en dos módulos.

**Gano** que el contexto quede completo —`domain`/`data`/`ui`, como el catálogo— y que `:catalog:ui` no cargue
una pantalla que no habla de catálogo. `:app` sigue sin saber qué pantallas existen: `:favorites:ui` exporta
su `favoritesEntries()` y su `favoritesNavKeys()` igual que el catálogo, y el patrón de ADR-0006 §3 se valida
por segunda vez, que es cuando un patrón deja de ser una anécdota.

**Pago** un cuarto módulo en el bloque y un `Product.toCard()` duplicado —unas diez líneas— porque la regla 4
prohíbe `:favorites:ui → :catalog:ui`. Es duplicación real, no aparente: son dos mappers que pueden divergir.

**Cuándo elegiría otra cosa**: si la pantalla fuera lo único que el contexto va a tener nunca, meterla en
`:catalog:ui` es defendible y ahorra un módulo. Acá no aplica porque la Etapa 2 le suma a favoritos un
`FavoritesSyncWorker`, un estado `PENDING` visible y resolución de conflictos — todo eso necesita casa
propia, y moverlo después cuesta más que ponerlo bien ahora.

### 2. La trampa de `AppErrorStrings` no se dispara: la pantalla de favoritos no tiene estado de error

§1.2 dejó el aviso escrito cuando descartó `:core:ui`:

> `AppErrorStrings` nace en `:catalog:ui`. Ojo con la trampa: `:favorites:ui` **no puede importarlo** de ahí,
> porque la regla 4 restringe el cruce entre contextos al nivel `domain`. O se duplica, o se promueve.

Al diseñar la pantalla resulta que **no hace falta ninguna de las dos**. Favoritos lee de Room y no refresca
nada: no tiene `RefreshCatalog`, no tiene `NetworkMonitor`, no tiene un `Either` del que extraer un
`AppError`. Sus estados son `Loading` / `Empty` / `Ready`, y ninguno lleva texto de error.

Esto no es un truco para esquivar la regla; es la regla funcionando. El aviso de §1.2 daba por hecho que toda
feature muestra errores de red, y esta feature no toca la red.

**Gano** no revivir `:core:ui` por anticipado ni duplicar textos que hoy no divergen. Y gano una respuesta
concreta a la pregunta que §1.2 dejó abierta, en vez de arrastrarla.

**Pago** que el disparador siga pendiente y haya que volver a evaluarlo, en vez de cerrarlo de una vez.

**Cuándo elegiría otra cosa**: en la Etapa 2, cuando `FavoritesSyncWorker` le dé errores propios a favoritos
—conflicto resuelto, sync fallido— habrá dos features con `AppError` en pantalla y textos que sí divergen
(«No pudimos cargar el catálogo» vs. «No pudimos guardar el favorito»). **Ese** es el momento de promover, y
el argumento de §1.2 sobre que el mapeo *probablemente no debe compartirse* seguirá siendo el correcto: lo
que se promueve es la forma (`título` + `cuerpo`), no las cadenas.

### 3. La tabla nace con `updatedAt` y `syncState`, y la migración es `@AutoMigration(1 → 2)`

§1.5 pide que la tabla nazca con las dos columnas de sync aunque en la Etapa 1 nunca cambien de valor, para
no pagar una migración de Room después. Se cumple al pie de la letra:

```kotlin
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val productId: Int,
    val updatedAt: Long,        // epoch millis UTC — el reloj inyectado, no System.currentTimeMillis()
    val syncState: SyncState,   // en Etapa 1, siempre LOCAL_ONLY
)

enum class SyncState { LOCAL_ONLY }
```

Room 2.8.4 genera la migración sola cuando el cambio es *agregar una tabla*, y la valida contra los schemas
exportados. No hay `Migration` que escribir a mano ni `fallbackToDestructiveMigration` que justificar.

**Gano** cero migración manual, un `2.json` commiteado como nuevo baseline, y ninguna migración de Room en la
Etapa 2 cuando el worker empiece a escribir esas dos columnas. Gano también que la primera migración del
proyecto ocurra ahora, con una app que nadie instaló todavía, y no cuando duela.

**Pago** dos columnas que en la Etapa 1 son constantes, y un `enum` con **un solo caso**. Alguien va a
preguntar por las dos cosas, y la respuesta tiene que ser mejor que «después lo voy a necesitar»: la columna
es lo que evita la migración; el caso no. Room serializa enums a `TEXT` de fábrica, así que sumar `PENDING` y
`SYNCED` en la Etapa 2 no cuesta migración alguna. Declararlos hoy sería código muerto sin contrapartida.

**Cuándo elegiría otra cosa**: sin Etapa 2 a la vista, la tabla sería `favorites(productId)` y nada más — dos
columnas especulativas en un esquema que nadie va a sincronizar son exactamente el tipo de generalidad
prematura que este proyecto evita en otros lados (ver ADR-0003 con `Money`). La diferencia es que acá el
consumidor está planificado y fechado, no imaginado.

### 4. El toggle es atómico en el DAO y **descarta** el `Boolean` que manda la UI

`ProductGrid` entrega `onFavoriteClick: (Int, Boolean) -> Unit` — el `checked` que el usuario acaba de ver.
El caso de uso es `ToggleFavorite(id)` y ese boolean se ignora:

```kotlin
@Transaction
suspend fun toggle(favorite: FavoriteEntity) {
    if (count(favorite.productId) > 0) deleteById(favorite.productId) else insert(favorite)
}
```

La autoridad es la base, no el estado que la pantalla tenía dibujado. Y el `@Transaction` cierra la ventana
de carrera entre el `SELECT` y el `INSERT`/`DELETE`, que con dos taps rápidos es alcanzable de verdad.

**Gano** que un tap sobre una grilla que todavía no recompuso no pueda escribir el valor equivocado. Es el
mismo principio que gobierna §2.1: **una sola fuente de verdad, y no es la UI**.

**Pago** un parámetro del design system que se ignora, lo que a primera vista parece un descuido y hay que
explicar en el código.

**Cuándo elegiría otra cosa**: con un backend que exponga `PUT /favorites/{id} {favorite: true}`, un
`setFavorite(id, desired)` idempotente encaja mejor con el reintento del worker — reintentar un `toggle` es
peligroso, reintentar un `set` no. La Etapa 2 puede revisitarlo, y la firma del design system ya lo permite
sin tocar la pantalla. Que el seam del DS entregue el boolean **no es un error del DS**: es la firma correcta
para el mundo al que vamos, y hoy sencillamente no la necesitamos.

### 5. `ToggleFavorite` devuelve `Unit`, no `Either<AppError, Unit>`

**Gano** un caso de uso honesto. En la Etapa 1 la escritura es local, Room es el SSOT y la UI reacciona al
`Flow`: no hay error que mostrar. Un `SQLiteException` al escribir una fila de tres columnas es excepcional
de verdad, no un estado de UI, y modelarlo como `Either` obligaría a cada pantalla a manejar una rama que
nunca se ejecuta.

Corolario directo: **`savingFavoriteIds` del design system queda en `emptySet()`**. Ese parámetro existe para
el estado `PENDING` de la Etapa 2; usarlo ahora sería inventar una latencia que no existe.

**Pago** que la firma parezca ingenua al lado de `RefreshCatalog`, que sí devuelve `Either`.

**Cuándo elegiría otra cosa**: aquí está lo interesante — **tampoco cambia en la Etapa 2**. El toggle seguirá
escribiendo local primero (optimista, decisión 12 de `ARCHITECTURE.md`) y el error remoto será del worker, no
del usuario. `Either` haría falta solo si la escritura fuera remota-primero, que es justo lo que §2.5 de la
Etapa 2 descarta. La firma es estable, no provisoria.

### 6. El cruce catálogo × favoritos se hace en memoria, en las dos pantallas

§2.2 ya lo decidió para la grilla: sin foreign keys, `combine` y un `Set<ProductId>` para búsqueda O(1). La
pantalla de favoritos usa exactamente la misma herramienta —`combine(ObserveCatalog(null), ObserveFavoriteIds())`
y filtrar— en vez de agregar un `observeByIds(ids)` a `CatalogRepository`.

**Gano** cero superficie nueva en el repositorio del catálogo y, sobre todo, que **ninguna query conozca las
dos tablas**. Es lo que hace que los contextos se puedan eliminar sin romper integridad referencial en disco,
que es la promesa entera de §2.2.

**Pago** un scan O(n) sobre el catálogo completo en cada emisión. Con 20 productos es ruido; conviene decirlo
antes de que lo pregunten.

**Cuándo elegiría otra cosa**: con paginación, un `WHERE productId IN (:ids)` — pero eso exige que una query
conozca las dos tablas, que es lo que §2.2 rechaza, así que la salida correcta sería otra: que `:favorites`
guarde lo que necesita mostrar. El disparador real es la paginación, no el tamaño del catálogo.

**Efecto lateral que se documenta, no se esconde**: un favorito cuyo producto ya no está en `products` —la
API lo retiró y `deleteMissing` lo podó— simplemente **no aparece** en la pantalla. Sin FK no hay cascada, y
la fila huérfana sobrevive en la tabla. Es correcto para la Etapa 1: quien tiene contexto para decidir si esa
fila se borra es el sync, y el sync todavía no existe.

## Consecuencias

- **El diseño inicial cambia en tres lugares**: los cuatro ítems de favoritos se cierran, el ítem de la tabla se
  cierra), §1.2 (`:core:database` deja de estar «acotado a productos»; `:core:testing` se cierra al final del
  bloque). La Etapa 1 pasa a **17 módulos**, 18 con `:core:testing`.
- **La regla 4 queda ejercida en ambas direcciones y es verificable en el classpath**, que es lo que la
  Etapa 4 va a automatizar. `:favorites:ui` no debe ver Room, Retrofit ni `:catalog:ui`.
- **`:favorites:domain` es más chico que `:catalog:domain`**: no depende de `:core:common`, porque sin
  `Either` (§5) no hay nada que importar de ahí. Que el módulo encoja es la prueba de que el contexto está
  bien acotado, no de que falte algo.
- **`:core:testing` por fin tiene su segundo consumidor.** ADR-0006 dejó `MainDispatcherExtension` inline en
  `:catalog:ui/src/test` con la regla explícita de que nacería con `:favorites:ui`. Nace. Y el guard de ciclos
  que §1.2 no podía escribir —«cuáles módulos hay que excluir depende de qué termine dependiendo ese módulo»—
  resulta ser **solo el módulo mismo**: sus únicas dependencias son `kotlinx-coroutines-test` y JUnit, ambas
  externas.
- **`FakeNetworkMonitor` no sube a `:core:testing`.** Necesitaría `:core:connectivity`, y eso convertiría un
  módulo JVM puro en un módulo Android por un fake de dos líneas con un solo consumidor. Mismo criterio, leído
  al revés.
- **`:app` gana dos dependencias**: `:favorites:ui` y `:favorites:data` —esta última solo para que Hilt
  agregue sus `@Module`, igual que `:catalog:data`—.
- **Deuda consciente**: la fila huérfana de §6 y el `savingFavoriteIds` sin usar de §5. Las dos se resuelven
  en la Etapa 2 y las dos están escritas acá para que no se descubran como sorpresas.

## Notas de implementación

- **`androidx.room.AutoMigration` verificado en el artefacto**, no de memoria: está en
  `room-common-jvm-2.8.4.jar`, junto con `DeleteTable` y `BuiltInTypeConverters`. Room genera la migración sin
  spec cuando el cambio es agregar una tabla; borrar o renombrar sí exigiría `@DeleteColumn`/`@RenameColumn`.
- **Ninguna versión nueva en `libs.versions.toml`.** Es el primer bloque del proyecto que no toca el catálogo.
- **Cuidado con el `Clock`.** `CatalogDataModule.provideClock()` es un `@Provides` sin scope, y aunque el
  módulo sea `internal object`, el binding es global. `:favorites:data` lo **consume**; declarar otro sería un
  duplicate binding.
- **Aridad de `combine`.** `CatalogViewModel` ya combina cinco flujos y los overloads tipados llegan hasta
  ahí. El sexto —los favoritos— se pliega dentro del `flatMapLatest` que ya existe, con un `data class`
  privado. La alternativa, `combine(vararg)` con destructuring de array, compila pero pierde los tipos.
- **`ProductDetailViewModel` sí tiene lugar**: pasa de cuatro flujos a cinco y no necesita reestructurarse.
- El `LocalDataSource` de `:favorites:data` se declara como interfaz por el mismo motivo que en ADR-0005:
  mantiene Robolectric confinado a `:core:database` y deja el test del repositorio en Jupiter puro.
- Los dos casos de uso de `:favorites:domain` **no llevan test**: son `fun interface` de delegación pura.
  `ObserveCategories` tiene test porque deriva y ordena; acá no hay lógica que probar. Mismo criterio que dejó
  `:core:connectivity` sin tests.
