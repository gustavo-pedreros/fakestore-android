# ADR-0006: La capa de presentación del catálogo — la caché decide, y la navegación es estado

## Estado

Aceptada — 2026-08-23. Cierra la capa de presentación del catálogo y convierte la tabla de estados en código. **Corrige dos
asignaciones de §1.2**: `NetworkMonitor` no va en `:core:network` (§1), y la Etapa 1 son **14 módulos**,
no 13. Supera la fila «UI» de §3 (Navigation Compose → Navigation 3). **Corregida — 2026-08-24** (ver
[Corrección](#corrección--2026-08-24)): tres desviaciones de implementación, y **D4 se cumplió** al
construir `:favorites` ([ADR-0007](0007-favorites-context.md)) — el corazón dejó de ser decorativo.

## Contexto

El grafo de Hilt del catálogo está completo y **no lo consume nadie**. ADR-0005 dejó `CatalogRepositoryImpl`
leyendo de Room y escribiendo desde la red, con los cinco casos de uso bindeados. `:core:designsystem` tiene
dibujadas las dos pantallas enteras, hasta el último átomo. Y sin embargo `:app` depende solo del design
system, y `MainActivity` renderiza `demo/DesignSystemCatalog.kt`: datos hardcodeados, `remember {
mutableStateOf }` en vez de ViewModel, y un `selectedId: Int?` haciendo de router.

Este bloque es la costura entre las dos mitades. Lo interesante es cuánto **no** hay que decidir: el design
system ya fijó la forma de la presentación cuando se construyó.

| Pieza del DS | Firma | Qué deja resuelto |
|---|---|---|
| `FsUiState<out T>` | `Loading` / `Empty` / `Failure(String)` / `Content(T, fromCache)` | los cuatro estados visuales; falta **quién los deriva** |
| `FsStateHost` | `(state, loading, empty, failure, modifier, content)` | el `when` exhaustivo no se reescribe |
| `ProductGrid` | `(products, favoriteIds: Set<Int>, …, savingFavoriteIds)` | favoritos entra sin tocar la pantalla |
| `FsStatusBanner` | `(visible, message, icon)` | el mensaje lo pone la feature: ahí va la antigüedad |
| `CategoryFilterRow` | `(categories: List<String>, selected: String?, onSelect)` | habla `String`, no `Category` |
| `ProductCardUiModel` | `id: Int`, `price: Double`, `imageUrl: String?` | modelo espejo (D3 del plan del DS): hay un mapper |

Lo que el DS **no** trae y este bloque escribe es exactamente lo que §1.2 le asignó a `:catalog:ui` cuando
descartó `:core:ui`: los textos de error, el `Reintentar`, y el formato de la antigüedad del caché.

Las cuatro decisiones que siguen son las que quedan.

## Decisión

### 1. `NetworkMonitor` no va en `:core:network`: nace `:core:connectivity`

El diseño inicial lo colocaba en `:core:network` —"queda pendiente, para cuando se construya
`:catalog:data`/`:catalog:ui`"—. Esa asignación se tomó antes de saber **quién** iba a consumirlo, y ese dato
la invalida.

`:core:network` tiene una identidad concreta —*el cliente HTTP*: Retrofit, OkHttp, `executeCall`, la ACL de
errores— y un único consumidor, `:catalog:data`. La conectividad la consumen otros:

| Consumidor | Para qué | Cuándo |
|---|---|---|
| `:catalog:ui` | el reintento automático de §2.5 | este bloque |
| `:favorites:data` | `FavoritesSyncWorker` | Etapa 2 |
| `:core:sdui` | el fallback ladder | Etapa 2 |

**Un módulo consumido por `ui` y por `data` a la vez no puede ser honestamente parte de la capa de datos.**
Eso es la definición de transversal, y es el argumento entero.

```
core/connectivity/                     // android.library + hilt. Sin Retrofit, sin Room, sin Compose.
├── AndroidManifest.xml                // ACCESS_NETWORK_STATE
├── NetworkMonitor.kt                  // interface { val isOnline: Flow<Boolean> }
├── ConnectivityNetworkMonitor.kt      // internal, callbackFlow sobre ConnectivityManager
└── di/ConnectivityModule.kt           // @Binds
```

**Gano** que la arista nueva de la UI tenga una superficie de **exactamente `Flow<Boolean>`**. Con
`:core:network` la arista también era técnicamente inocua —Retrofit está declarado `implementation`, no se
filtra al `debugCompileClasspath` de la UI— pero era un argumento que había que hacer cada vez. Acá no hay
nada que argumentar: el módulo entero son dos tipos. Y el permiso `ACCESS_NETWORK_STATE` viaja con el código
que lo usa, misma lección que el commit `36983e9` («declare INTERNET where the app can actually get it»).

**Pago** el módulo #14, para tres archivos. §1.2 cerró la Etapa 1 en 13 módulos justamente por descartar
`:core:ui` con el argumento de que «un módulo Gradle para un objeto es ceremonia». Hay que explicar por qué
acá no aplica: aquello era un objeto de Kotlin **puro, sin dependencias**, cuyo contenido era un `when` de
cuatro líneas. Esto es un adaptador del framework con un permiso, un binding de Hilt y un `callbackFlow` con
ciclo de vida propio. La ceremonia no se mide en archivos, se mide en si el módulo tiene una frontera real
que defender.

**Cuándo elegiría otra cosa**: si la conectividad la consumiera **solo** la capa de datos, viviría en
`:core:network` sin discusión. Y en un proyecto sin Etapa 2 a la vista, empezar en `:core:network` y partir
después también es defendible — el split cuesta mover tres archivos y cambiar un import.

**Consecuencia sobre la regla 3 de §1.3.** Deja de leerse como lista cerrada:

> `*:ui` depende de `*:domain` (nunca de `*:data`), de `:core:designsystem`, y de los `:core:*` técnicos que
> no traen UI ni persistencia.

Es una ampliación, no una excepción, y es verificable en el classpath — que es lo que la Etapa 4 hará
mecánicamente.

### 2. El ViewModel no habla de `String`; el mapeo a `FsUiState` ocurre en el borde composable

`FsUiState.Failure` carga un `String` ya resuelto. Si el ViewModel lo produjera, necesitaría `Context`, y los
tests de §2.5 —que son el entregable central de este bloque— pasarían a necesitar Robolectric.

```kotlin
sealed interface CatalogContent {
    data object Loading : CatalogContent
    data object Empty : CatalogContent
    data class Ready(val products: List<ProductCardUiModel>) : CatalogContent
    data class Failure(val error: AppError, val offline: Boolean) : CatalogContent
}
```

El composable traduce `CatalogContent → FsUiState<List<ProductCardUiModel>>` resolviendo los textos con
`stringResource`. Es el mismo movimiento que ADR-0005 §1 hizo con los datasources: se compra un seam para
que el test sea puro.

La reducción **es** la tabla de §2.5, y el orden de las ramas es la invariante:

```kotlin
private fun reduce(
    products: List<Product>,
    lastSyncedAt: Instant?,
    refresh: RefreshState,          // Idle | InFlight | Failed(AppError)
    isOnline: Boolean,
): CatalogContent = when {
    products.isNotEmpty() -> CatalogContent.Ready(products.map(Product::toCard))
    refresh is Failed     -> CatalogContent.Failure(refresh.error, offline = !isOnline)
    lastSyncedAt != null  -> CatalogContent.Empty     // el servidor respondió, y no hay catálogo
    else                  -> CatalogContent.Loading
}
```

Tres detalles cierran los casos:

- **`products.isNotEmpty()` va primero.** Ésa es la frase «un fallo de refresh nunca debe tapar datos que el
  usuario ya tenía» escrita como código, y no como comentario. `isStale = refresh is Failed &&
  products.isNotEmpty()` alimenta la banda.
- **`lastSyncedAt` distingue `Empty` de `Loading`** sin un flag extra. Si nunca hubo un sync exitoso, no
  sabemos que el catálogo esté vacío — solo que todavía no cargó. Es el uso que justifica retroactivamente
  que ADR-0005 §3 metiera `sync_metadata` en el bloque anterior: se escribió para la banda, y termina
  desambiguando dos estados.
- **El reintento automático** se generaliza una línea respecto de §2.5, que dice «si la caché está vacía, se
  dispara `RefreshCatalog` solo». Acá se dispara **si el último refresh falló**, con o sin caché: cubre el
  caso de la tabla y además hace que la banda de antigüedad desaparezca sola al volver la red.

**Gano** que `CatalogViewModelTest` sea Jupiter puro: sin Android, sin Robolectric, con los dobles como
lambdas —`ObserveCatalog { flowOf(products) }`— gracias a que ADR-0004 §3 los hizo `fun interface`. Las siete
filas de §2.5 se asertan sobre `data class`es.

**Pago** un tipo que se parece mucho a `FsUiState` y un `when` de cuatro ramas por pantalla.

**Cuándo elegiría otra cosa**: si el design system expusiera `Failure(@StringRes Int)` en vez de `String`, el
ViewModel podría emitir `FsUiState` directo y `CatalogContent` sobraría. Es una API que vale la pena
considerar si aparece una tercera pantalla con estados.

### 3. Navigation 3, y la feature es dueña de sus entries **y de sus keys**

Se usa **Navigation 3** (`androidx.navigation3:1.1.6`, estable desde el 12 de agosto de 2026), no Navigation
Compose 2.x como decía §3. No es un bump de versión: es otro modelo.

En Nav2 el back stack es estado oculto dentro de un `NavController` y los argumentos viajan como `Bundle`
hasta un `SavedStateHandle`. En Nav3 **el back stack es una `SnapshotStateList` que la app posee**, las keys
son objetos tipados, y el argumento llega al constructor del ViewModel sin serializarse por el camino.

```kotlin
// :catalog:ui — navigation/CatalogNavigation.kt
@Serializable data object CatalogKey : NavKey
@Serializable data class ProductDetailKey(val productId: Int) : NavKey

fun EntryProviderScope<NavKey>.catalogEntries(
    onProductClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onFavoritesClick: () -> Unit,
) {
    entry<CatalogKey> { CatalogScreen(onProductClick, onFavoritesClick) }
    entry<ProductDetailKey> { key ->
        val viewModel = hiltViewModel<ProductDetailViewModel, ProductDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(key.productId) },
        )
        ProductDetailScreen(viewModel, onBackClick)
    }
}
```

`:app` compone y navega mutando una lista:

```kotlin
val backStack = rememberNavBackStack(NavKeyConfiguration, CatalogKey)

NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),      // sin esto no hay ViewModel por entry
    ),
    entryProvider = entryProvider {
        catalogEntries(
            onProductClick = { id -> backStack.add(ProductDetailKey(id)) },
            onBackClick = { backStack.removeLastOrNull() },
            onFavoritesClick = { /* TODO(:favorites §1.4) */ },
        )
    },
)
```

**El detalle que casi rompe la decisión.** `rememberNavBackStack` sobrevive a la muerte del proceso
serializando el back stack, y para eso exige un `SavedStateConfiguration` cuyo `serializersModule` registre
`polymorphic(NavKey::class)` con **cada subclase concreta**. Leído literal, eso obliga a `:app` a enumerar
las keys de todas las features — es decir, a saber que existe una pantalla de detalle, que es exactamente lo
que esta decisión promete evitar. La salida es que la feature exporte también su registro:

```kotlin
// :catalog:ui
fun PolymorphicModuleBuilder<NavKey>.catalogNavKeys() {
    subclass(CatalogKey::class)
    subclass(ProductDetailKey::class)
}

// :app — una línea por feature, y ninguna key nombrada
internal val NavKeyConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule { polymorphic(NavKey::class) { catalogNavKeys() } }
}
```

Cada feature aporta entonces **dos** extensiones —sus entries y sus keys— y `:app` no nombra ni una pantalla.

**Gano** tres cosas concretas. La primera es que el argumento del detalle es tipado de punta a punta:
`ProductDetailKey(productId)` entra por `@AssistedInject` al ViewModel y **`SavedStateHandle` deja de ser el
canal de argumentos**, con lo que desaparece la clase entera de bugs de claves mal escritas. La segunda es
que el back stack, al ser una lista observable, se testea y se manipula como cualquier estado. La tercera es
que `entryProvider` tiene un parámetro `fallback` para keys desconocidas: es, literalmente, el escalón de un
fallback ladder, y rima con la decisión #10 de `ARCHITECTURE.md` cuando llegue SDUI.

**Pago** que Nav3 es joven —1.1.6, no 2.9.x— y que la comunidad todavía escribe casi todo en Nav2, así que
hay menos respuestas hechas cuando algo falla. Y pago las tres dependencias en un módulo de feature.

**Cuándo elegiría otra cosa**: con dos pantallas y un equipo que ya domina Nav2, quedarse en
`navigation-compose` es perfectamente correcto y ahorra la curva. La razón para no hacerlo acá es que el
proyecto tiene Etapas 2 y 3 encima —SDUI necesita construir destinos desde datos, y un back stack que es
una lista se presta a eso mucho mejor que un grafo declarado en tiempo de compilación—.

**Nota sobre el artefacto de Hilt.** Se usa `androidx.hilt:hilt-lifecycle-viewmodel-compose`, no
`hilt-navigation-compose`: desde 1.3.0 `hiltViewModel()` vive ahí precisamente para no arrastrar
`androidx.navigation`. Con Nav3, arrastrar Nav2 sería absurdo.

### 4. El corazón se renderiza, pero está desconectado

`:favorites` no existe todavía. `ProductGrid` ya recibe `favoriteIds: Set<Int>`, así que el seam está puesto:
en este bloque se pasa `emptySet()` y `onFavoriteClick` es un no-op con `TODO(:favorites §1.4)`.

**Gano** honestidad con el staging: el requisito 3 se demuestra cuando está hecho. El bloque `:favorites`
solo agrega un `combine(catalogo, favoriteIds)` en el ViewModel — ni `CatalogScreen` ni
`ProductDetailScreen` se tocan, que es lo que §2.2 prometió cuando decidió no poner foreign keys.

**Pago** que en la demo intermedia el corazón no responde al tap.

**Cuándo elegiría otra cosa**: nunca un estado local en memoria «para que se vea vivo». Haría parecer
cumplido justo el requisito que pide persistencia entre reinicios, y habría que borrarlo dos bloques después.

## Consecuencias

- **El diseño inicial cambia en cuatro lugares**: nace `:core:connectivity` (el `NetworkMonitor` sale de
  `:core:network`; 14 módulos y no 13), §1.3 (la regla 3 se amplía; los ítems `ui:` y `:app:` se cierran),
  §3 (la fila «UI» pasa a Navigation 3; Turbine deja de estar pendiente).
- **`:catalog:ui` no ve Room, Retrofit ni OkHttp.** Verificable con `:dependencies`, no de palabra.
- **`:app` no depende de `:catalog:domain`.** Las lambdas de navegación hablan `Int`, igual que
  `ProductGrid.onProductClick`; el dominio queda confinado a `data` y `ui`.
- **`SavedStateHandle` no aparece en el proyecto.** El argumento del detalle viaja tipado (§3).
- ~~`:core:testing` sigue diferido…~~ **Resuelto**: nació con `:favorites:ui` como segundo consumidor, tal
  como se predijo, y `MainDispatcherExtension` se movió ahí desde `:catalog:ui/src/test`.
- **`Formatters` sigue diferido.** El formato de la antigüedad queda privado en `:catalog:ui`, con
  `java.time` — disponible sin desugaring gracias al `minSdk = 26` que fijó D6 del plan del design system.
- ~~Deuda consciente: el corazón sin conectar (§4) y el `onFavoritesClick`…~~ **Saldada**: `:favorites` se
  construyó completo — ver [D4 se cumplió](#d4-se-cumplió-el-corazón-quedó-conectado).

## Notas de implementación

- Las versiones se verificaron contra el índice Maven de Google el 2026-08-23:
  `androidx.navigation3:1.1.6` (estable; `1.2.0-alpha07` en curso),
  `androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0` —que comparte el `lifecycleRuntimeKtx` que el
  catálogo ya tenía—, `androidx.hilt:hilt-lifecycle-viewmodel-compose:1.4.0` y `app.cash.turbine:1.2.1`.
- `NavDisplay` trae por defecto **solo** `rememberSaveableStateHolderNavEntryDecorator()`. El
  `rememberViewModelStoreNavEntryDecorator()` hay que pasarlo explícitamente o cada navegación reusa el mismo
  `ViewModel`.
- El caso «detalle sin caché» de §2.5 es alcanzable de verdad: `ObserveProductDetail` lee de Room y devuelve
  `null` si no está. Entrando desde la lista siempre está; el estado existe para deep links y muerte de
  proceso, y se resuelve con el mismo error bloqueante con `Reintentar`.
- **MockK sigue sin entrar.** ADR-0004 §3 hizo los casos de uso `fun interface`, así que los dobles son
  lambdas y `FakeNetworkMonitor` es un `MutableStateFlow` envuelto. Turbine sí entra: es el primer módulo con
  `StateFlow` que asertar.

## Corrección — 2026-08-24

Tres cosas que la implementación cambió respecto de lo decidido arriba. Ninguna toca las cuatro decisiones;
las tres son consecuencias que no se vieron al escribirlas.

### `navigation3-runtime` y `kotlinx-serialization-core` son `api`, no `implementation`

§3 dio por hecho que `:catalog:ui` podía declararlas como `implementation`. No puede: las dos funciones que
la decisión expone —`EntryProviderScope<NavKey>.catalogEntries(...)` y
`PolymorphicModuleBuilder<NavKey>.catalogNavKeys()`— tienen esos tipos en su **firma pública**, así que `:app`
los necesita en su classpath de compilación para poder componer.

**Gano** que el contrato sea honesto: si la feature es dueña de su grafo de navegación (§3), la navegación es
parte de su API, y Gradle debe decirlo. **Pago** que Navigation 3 quede visible para todo consumidor de
`:catalog:ui` — que hoy es `:app` y nada más. **Cuándo elegiría otra cosa**: si `catalogEntries` recibiera un
`EntryProviderScope` envuelto en un tipo propio de la feature, las dos volverían a `implementation` a cambio
de una capa de indirección que ahora mismo no compra nada.

### `ProductDetailViewModel` no refresca al entrar

El plan del bloque decía «ausente + sin sync → skeleton», sin decir quién dispara el sync. La lectura literal
—refrescar en `init`, como hace `CatalogViewModel`— significa **pedir el catálogo completo en cada tap sobre
un producto**, y el detalle se alcanza desde una lista que acaba de refrescarse.

La implementación mira primero: `if (observeProductDetail(id).first() == null) launchRefresh()`. Refresca solo
cuando el producto falta, que es exactamente el caso que el estado existe para cubrir (deep link, muerte de
proceso). El botón **Reintentar** y el reintento automático al recuperar conexión siguen igual.

**Gano** que la ruta común no gaste red. **Pago** una colección extra en `init` y un test más para probar que
la ruta común no refresca. **Cuándo elegiría otra cosa**: si el detalle tuviera su propio endpoint —
`GET /products/{id}`—, refrescar al entrar sería barato y correcto; acá el único refresh disponible es el del
catálogo entero.

### El snackbar no estrena copy propio

§2 dejó el canal `Channel<AppError>` para el fallo de un refresh pedido por el usuario, sin decidir qué texto
mostrar. La pantalla reusa `appErrorStrings(error, offline = false).body` — el mismo cuerpo del error
bloqueante. Para `AppError.Network`, que es el caso real de un pull-to-refresh sin red, eso es exactamente
«Revisa tu conexión y vuelve a intentarlo.»

**Gano** un string menos y que la causa llegue al usuario en vez de un genérico. **Pago** que el snackbar
hable en la voz de una pantalla completa, que es un registro más formal del que un snackbar suele usar.
**Cuándo elegiría otra cosa**: en cuanto haya una segunda superficie no bloqueante, el par título/cuerpo se
queda corto y `AppErrorStrings` necesita una tercera variante, corta.

### Nota menor

`staleMessage()` y `CenteredBlock()` nacieron privados en `CatalogScreen.kt` y salieron a
`component/StateScaffolding.kt` cuando `ProductDetailScreen` los necesitó: el segundo consumidor, que es el
mismo criterio con el que este ADR difirió `Formatters` y `:core:testing`.

### D4 se cumplió: el corazón quedó conectado

`:favorites` se construyó ([ADR-0007](0007-favorites-context.md)) y el `combine(catalogo, favoriteIds)` que
D4 prometía es hoy `CatalogSlice` en `CatalogViewModel` — un `data class` en vez de un `Pair` porque
`combine` ya estaba en los cinco flujos del overload tipado, no por un cambio de diseño. Ni `CatalogScreen`
ni `ProductDetailScreen` se reescribieron: ambos solo dejaron de pasar `emptySet()`/no-op y empezaron a
pasar el estado y el handler reales. La predicción de D4 se sostuvo exactamente como se escribió.
