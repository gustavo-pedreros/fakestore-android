# Arquitectura de FakeStore Android

Este documento explica **cómo está construida la app y por qué**. Las decisiones puntuales, con su
evidencia y su costo, viven en los [ADR](#índice-de-adr); acá está el mapa que las conecta.

---

## 1. Qué resuelve

Un catálogo de productos offline-first sobre `https://fakestoreapi.com`, con cuatro capacidades:

1. **Lista de productos** con imagen, título y precio, más sus estados de carga y de error.
2. **Detalle** con imagen grande, título, descripción, precio y categoría.
3. **Favoritos que sobreviven al reinicio** de la app y a la muerte del proceso.
4. **Caché offline con indicador visual** de que los datos mostrados vienen de disco.

Sobre esa base se diseñaron dos capacidades más —una API propia y server-driven UI— que quedaron
**diseñadas y no construidas**. Están en [«Diseñado, no construido»](#5-diseñado-no-construido), con el detalle de por qué el
diseño elegido es el que hace compatibles SDUI y offline-first.

---

## 2. Dos ideas ordenan todo

- **DDD para los límites entre módulos** → cada *bounded context* es un grupo de módulos Gradle.
- **Clean Architecture dentro de cada contexto** → `ui → domain ← data`, con inversión de dependencias.

Al ser DDD ortodoxo, **las capas son módulos Gradle, no paquetes**. La consecuencia práctica es la que
importa: `domain` no puede importar Retrofit ni Room porque no los tiene en el classpath. La disciplina la
impone la herramienta, no la buena voluntad ni la revisión de código.

### Grafo de módulos

```
:app                     Composition root: grafo de Hilt, NavHost, tema.

:catalog:domain          Kotlin puro. Product, CatalogRepository (interfaz), casos de uso.
:catalog:data            Retrofit + Room. DTO→dominio (ACL). CatalogRepositoryImpl.
:catalog:ui              Compose + ViewModels + UiState + UiMappers.

:favorites:domain        Kotlin puro. FavoritesRepository, ToggleFavorite, ObserveFavoriteIds.
:favorites:data          Room. Escritura optimista local.
:favorites:ui            Pantalla de favoritos.

:shared:kernel           Kotlin puro. Lenguaje ubicuo: ProductId, AppError.
:core:common             Either, DispatcherProvider, Formatters, Clock. Kotlin puro.
:core:connectivity       NetworkMonitor (ConnectivityManager → Flow<Boolean>).
:core:network            Retrofit/OkHttp/kotlinx.serialization, executeCall, ACL de errores.
:core:database           Instancia física única de Room + entities + DAOs + migraciones.
:core:designsystem       Tema M3, tokens, átomos, moléculas, organismos, FsIcons, test tags.
:core:testing            Extensiones JUnit5, fakes, factories.
```

`:core:connectivity` no vive dentro de `:core:network` porque lo consumen `:catalog:ui` y, en el diseño de
la etapa siguiente, también `:favorites:data` y `:core:sdui`. Un módulo que sirve a `ui` y a `data` a la vez
no puede ser honestamente capa de datos ([ADR-0006, Decisión 1](adr/0006-catalog-presentation.md#1-networkmonitor-no-va-en-corenetwork-nace-coreconnectivity)).

El crecimiento previsto es **aditivo, no invasivo**: cada etapa agrega módulos y, a lo sumo, una arista de
dependencia en los existentes. Ninguna reescribe la anterior.

### Reglas de dependencia

1. `*:domain` es **Kotlin/JVM puro**: sin Android, sin Retrofit, sin Room, sin Compose.
2. `*:data` implementa las interfaces que declara su `*:domain`. Hilt cablea en runtime.
3. `*:ui` depende de `*:domain` (nunca de `*:data`), de `:core:designsystem`, y de los `:core:*` técnicos
   que no traen UI ni persistencia. Lo que la regla protege es que la presentación no toque Retrofit ni
   Room — no que tenga un número fijo de aristas.
4. **Cruce entre contextos: solo a nivel `domain`.** Un `data` jamás depende de otro contexto.
   - `:catalog:ui → :favorites:domain` — relación *Customer/Supplier*: el catálogo consume el flujo de
     favoritos para fusionarlo en la presentación.
   - `:favorites:ui → :catalog:domain` — el espejo: la pantalla de favoritos necesita saber cómo se ve un
     producto para dibujar sus tarjetas ([ADR-0007, Decisión 1](adr/0007-favorites-context.md#1-la-pantalla-de-favoritos-vive-en-favoritesui-que-consume-catalogdomain)). Es la primera vez
     que la regla se ejerce en las dos direcciones a la vez.
5. `:shared:kernel` es pequeño y **gobernado**: solo entra lo que dos o más contextos hablan de verdad.

Estas reglas hoy viven en la documentación y en la revisión. Verificarlas mecánicamente —tests de
arquitectura sobre el classpath— es trabajo pendiente, no algo que este repo ya haga.

---

## 3. Stack

| Área | Elección |
|---|---|
| Lenguaje / toolchain | Kotlin 2.3.0, JVM 21, `minSdk 26` — el desugaring deja de hacer falta |
| UI | Jetpack Compose (BOM 2026.08.00), Material 3, **Navigation 3** (1.1.6): back stack propio, claves tipadas y `entryProvider` por feature ([ADR-0006, Decisión 3](adr/0006-catalog-presentation.md#3-navigation-3-y-la-feature-es-dueña-de-sus-entries-y-de-sus-keys)) |
| Asincronía | Coroutines + Flow (`StateFlow`, `combine`, `flatMapLatest`) |
| DI | Hilt 2.60.1 + KSP 2.3.9. `hilt-lifecycle-viewmodel-compose` 1.4.0 en vez de `hilt-navigation-compose`, que arrastraría Navigation 2 |
| Red | Retrofit 3.0.0 (BOM) + OkHttp 5.4.0 (BOM) + kotlinx.serialization 1.10.0 |
| Persistencia | Room 2.8.4 (KSP) — última estable de la línea 2.x; la 3.0/KMP sigue en alpha ([ADR-0002](adr/0002-database-module.md)) |
| Imágenes | Coil 3, con caché en disco |
| Fechas | `kotlin.time.Instant`/`Clock` — stdlib, estable desde Kotlin 2.3 ([ADR-0005, Decisión 3](adr/0005-catalog-data-layer.md#3-lastsyncedat-entra-en-este-bloque-y-no-trae-ninguna-dependencia)) |
| Test | JUnit 5 (mannodermaus), `kotlinx-coroutines-test`, Turbine 1.2.1, MockWebServer. **Sin MockK**: los casos de uso son `fun interface`, así que los dobles son lambdas ([ADR-0004, Decisión 3](adr/0004-catalog-domain-model.md#3-la-forma-del-caso-de-uso-la-decide-su-contenido)). Para Room: Robolectric 4.16.1 + driver por defecto, con JUnit4 vía `junit-vintage-engine` ([ADR-0002](adr/0002-database-module.md), Corrección) |

---

## 4. Decisiones que atraviesan la app

### Room como fuente única de verdad

La UI **solo** lee de Room, vía `Flow`. La red **solo** escribe en Room; nunca alimenta la pantalla
directamente. Una flecha entra a la base, una flecha sale.

La consecuencia es que **no existe un "modo offline" en ninguna parte del código**: offline es simplemente
lo que la app hace cuando la flecha de escritura deja de dispararse y la de lectura sigue funcionando. El
requisito 4 deja de ser un parche y pasa a ser una propiedad de la arquitectura.

`:core:database` centraliza **una sola instancia física** de SQLite —menos memoria, batería y migraciones
que N bases— pero cada `*:data` consume **únicamente su propio DAO**, así los límites lógicos se mantienen.

### Sin foreign keys entre `products` y `favorites`

Favoritos guarda solo `productId`. El cruce ocurre reactivamente en el ViewModel, con
`combine(catalogFlow, favoriteIdsFlow)` y un `Set<ProductId>` para búsqueda O(1).

Se paga lógica en memoria. Se gana que los contextos evolucionen o se eliminen sin romper integridad
referencial en disco.

### "¿Hay caché?" decide si un error es bloqueante

El eje que gobierna toda la presentación del catálogo no es *"¿falló la red?"* sino **"¿hay datos
utilizables en Room?"**. Un fallo de refresh **nunca** debe tapar datos que el usuario ya tenía.

El ViewModel combina tres flujos: `ObserveCatalog()` (Room), el resultado del refresh en curso, y
`NetworkMonitor.isOnline`.

| Caché | Refresh | Conexión | Estado | UI |
|---|---|---|---|---|
| vacía | en curso | — | `Loading` | Skeletons |
| vacía | falló | offline | `Error(Network)` | Pantalla completa: *"Sin conexión…"* + Reintentar |
| vacía | falló | online | `Error(appError)` | Pantalla completa con el mensaje de la ACL + Reintentar |
| vacía | OK, 0 items | — | `Empty` | Estado vacío: el servidor respondió, pero no hay catálogo |
| con datos | en curso | — | `Content` | Contenido + indicador de refresco |
| con datos | falló | — | `Content` | Contenido + `FsStatusBanner` con la antigüedad + snackbar no bloqueante |
| con datos | OK | — | `Content` | Contenido fresco |

Detalles que cierran el caso:

- **Reintento automático al recuperar conexión.** `NetworkMonitor` emite la transición offline→online y, si
  el último refresh falló, dispara `RefreshCatalog` solo. El usuario no tiene que descubrir el botón, y la
  banda de antigüedad se cierra sola al volver la red.
- **Detalle sin caché.** `ProductDetailViewModel` **no** refresca al entrar: pedir el catálogo completo en
  cada tap sería un desperdicio. Solo dispara `RefreshCatalog` cuando el producto falta — que es exactamente
  el caso del deep link. Si un sync exitoso tampoco lo trae, el estado es `Unavailable`, no un error.
- **Imágenes.** La caché en disco de Coil las conserva offline; las que nunca se descargaron caen a
  placeholder con `contentDescription`, sin romper la fila.

Los estados de la tabla son **tests de ViewModel**, no verificación manual.

### Sin interceptor de mocks: la red real es la única fuente en debug

Servir fixtures desde `assets/` con un interceptor de OkHttp en debug es un patrón común. Acá no se hace.

Traería paridad de fixtures que mantener, un flag más en el grafo de Hilt y un camino de código que solo
existe en debug, a cambio de una comodidad que este proyecto no necesita: `fakestoreapi.com` es público y
estable, y los tests ya usan **MockWebServer**, que es determinista sin contaminar el binario.

**La consecuencia buscada** es que el arranque en frío sin caché y sin red sea un caso real que hay que
diseñar, no un escenario que el mock esconde. Es justamente lo que trata la tabla de arriba.

*Cuándo elegiría distinto*: con una API inestable, privada o todavía inexistente, el mock sí vale la pena.

### El toggle de favorito no le cree a la UI

El design system entrega el booleano que el usuario acaba de ver en el corazón. El caso de uso **lo
descarta**: abre una transacción, lee la tabla y decide. La autoridad es la base de datos, no el estado que
la pantalla tenía dibujado.

Se paga un parámetro del design system que se ignora, y que a primera vista parece un descuido. Se gana que
dos taps rápidos —o un tap sobre una grilla que todavía no recompuso— no puedan desincronizar nada.

### Precio como `Double`, formateado en el átomo

`price: Double` de punta a punta —`ProductEntity` (columna `REAL`), `Product`, `ProductCardUiModel`— y el
formateo dentro de `FsPriceText`.

En corto: la API manda `price` como número JSON, **no manda campo de moneda**, y en ninguna etapa del
diseño hay una sola operación aritmética sobre el precio. Sin aritmética no hay error de punto flotante que
acumular; sin moneda no hay semántica monetaria que modelar.

**Disparador para revisitarlo**: si la API mandara moneda, o si un backend propio agregara carrito, totales
o impuestos, `Money` vuelve — sobre `BigDecimal` o minor units, nunca sobre `Double`. La evidencia completa
está en [ADR-0003](adr/0003-price-representation.md).

---

## 5. Diseñado, no construido

Dos capacidades quedaron fuera por tiempo. Se dejan documentadas porque el diseño es la parte que se
defiende, y porque una de las dos decisiones es la que evita un choque frontal con el offline-first.

### SDUI con estructura y bindings, no contenido hidratado

El servidor envía **layout y bindings**; el cliente resuelve los bindings contra el **dominio cacheado en
Room**.

```jsonc
{ "screenId": "product_list", "contractVersion": 1,
  "root": { "type": "lazy_list", "id": "list", "source": "catalog",
            "itemTemplate": { "type": "product_card", "id": "card",
              "image": "{product.image}", "title": "{product.title}",
              "price": "{product.price}",
              "trailing": { "type": "favorite_toggle", "productId": "{product.id}" },
              "onTap": { "type": "navigate", "route": "product_detail",
                         "params": { "id": "{product.id}" } } } } }
```

El payload **no dice el título**: dice `{product.title}`. Y eso es lo que hace que SDUI y offline-first no
se peleen — layout cacheado en Room + datos cacheados en Room ⇒ la app renderiza server-driven **estando en
modo avión**. La alternativa habitual, el payload hidratado, obliga a cachear pantallas ya renderizadas y
rompe la fuente única de verdad.

Se paga complejidad en el cliente: hay que resolver bindings. El payload hidratado es más simple.

### El fallback ladder

El riesgo real de SDUI es que el servidor te deje sin UI. Cuatro niveles:

```
servidor  →  layout cacheado en Room  →  layout por defecto en assets  →  pantalla nativa
```

La app nunca queda en blanco. Se paga mantener vivo el nivel nativo — que es, precisamente, el trabajo ya
hecho: no se tira.

El layout empaquetado en `assets` es un JSON de respaldo del motor de render, no una capa de mocks de red:
no intercepta nada, es el último recurso.

### Compatibilidad hacia adelante del contrato

`sealed interface ComponentNode` con polimorfismo cerrado de kotlinx.serialization
(`@JsonClassDiscriminator("type")`), `ignoreUnknownKeys = true` y **deserializador por defecto →
`ComponentNode.Unknown`**. Un servidor nuevo **nunca** rompe un cliente viejo: el nodo desconocido se omite
del render en vez de tirar la pantalla.

### Sync de favoritos con LWW

Toggle → Room con `syncState = PENDING` y `updatedAt` en UTC; la UI reacciona al instante. Un
`FavoritesSyncWorker` (`unique work` con `KEEP`, `NetworkType.CONNECTED`, backoff exponencial) envía las
mutaciones pendientes; el servidor resuelve **Last-Write-Wins** por `updatedAt` y responde con el estado
autoritativo; el worker reconcilia Room, con **rollback silencioso** si el servidor ganó.

---

## 6. Las doce decisiones y su costo

La forma honesta de presentar una decisión es **qué gané, qué pagué, y qué haría distinto en otro
contexto**. Las últimas cuatro corresponden al diseño de [«Diseñado, no construido»](#5-diseñado-no-construido).

| # | Decisión | Por qué | Trade-off / cuándo NO lo haría |
|---|---|---|---|
| 1 | [**DDD ortodoxo: capa = módulo Gradle**](#2-dos-ideas-ordenan-todo) | La pureza de `domain` la garantiza el build, no la disciplina. Imposible importar Retrofit en un módulo que no lo tiene en el classpath. | ~20 módulos para 2 pantallas. Fricción de Gradle y sync más lento. En un equipo chico o un producto exploratorio usaría vertical slices con capas como paquetes. |
| 2 | [**Módulo = bounded context**](#2-dos-ideas-ordenan-todo) | Un contexto se entiende, se testea y se reemplaza entero. Crecer es agregar módulos, no tocar los existentes. Un equipo puede ser dueño de un contexto. | Exige definir contratos explícitos entre contextos y resistir la tentación del atajo. |
| 3 | [**Cruce entre contextos solo a nivel `domain`**](#reglas-de-dependencia) | El catálogo necesita saber qué es favorito. Permitir la arista en `domain` (Customer/Supplier) es honesto; permitirla en `data` sería un acoplamiento invisible. | Es una regla que hay que verificar, no solo escribir. De ahí que falten los tests de arquitectura. |
| 4 | [**Una sola base física de Room, DAOs separados**](adr/0002-database-module.md) | En móvil, N conexiones SQLite cuestan memoria, batería y migraciones. Los límites lógicos se mantienen porque cada `data` solo ve su DAO. | Rompe la pureza ortodoxa: hay entidades de varios contextos en un módulo técnico compartido. Pragmatismo móvil consciente. |
| 5 | [**Sin foreign keys entre `products` y `favorites`**](adr/0007-favorites-context.md#6-el-cruce-catálogo--favoritos-se-hace-en-memoria-en-las-dos-pantallas) | Acoplar tablas genera rigidez estructural. Los módulos evolucionan o se eliminan sin romper integridad referencial en disco. | Obliga a cruzar en memoria. Mitigado con `Set<ProductId>` para O(1) y `combine` reactivo. |
| 6 | [**Room como Single Source of Truth**](#room-como-fuente-única-de-verdad) | El requisito offline deja de ser un parche y pasa a ser una propiedad de la arquitectura: la UI solo lee de Room, la red solo escribe. | Toda escritura pasa por disco. Irrelevante a esta escala. |
| 7 | [**"¿Hay caché?" decide si un error es bloqueante**](adr/0006-catalog-presentation.md#2-el-viewmodel-no-habla-de-string-el-mapeo-a-fsuistate-ocurre-en-el-borde-composable) | Un fallo de red nunca debe tapar datos que el usuario ya tenía. Es la diferencia entre una app que se siente sólida y una que se siente rota. | Más estados que modelar y testear. Es exactamente lo que un interceptor de mocks habría escondido. |
| 8 | [**Sin interceptor de mocks**](adr/0001-networking-module.md#4-interceptors-solo-logging-debug) | Menos complejidad en el binario, ningún camino de código exclusivo de debug, y obliga a diseñar de verdad el arranque en frío sin red. Los tests usan MockWebServer, determinista y fuera del producto. | Se pierde la demo offline instantánea sin backend. Con una API inestable o privada, el mock sí valdría la pena. |
| 9 | [**SDUI con estructura + bindings, no contenido hidratado**](#sdui-con-estructura-y-bindings-no-contenido-hidratado) | Es lo único que hace compatibles SDUI y offline-first: layout cacheado + datos cacheados ⇒ render server-driven en modo avión. | Más complejidad en el cliente (resolver bindings). El payload hidratado es más simple pero rompe el SSOT. |
| 10 | [**Fallback ladder de 4 niveles**](#el-fallback-ladder) | El riesgo real de SDUI es que el servidor te deje sin UI. Servidor → caché → assets → nativo significa que la app nunca queda en blanco. | Hay que mantener el nivel nativo vivo. Se paga con trabajo que ya está hecho y no se tira. |
| 11 | [**Contrato compartido entre cliente y servidor**](#compatibilidad-hacia-adelante-del-contrato) | Si se es dueño de ambos extremos, escribir los DTOs dos veces garantiza drift. La ACL sigue existiendo: el dominio nunca ve un DTO. | Acopla cliente y servidor al wire format. Con un proveedor externo no lo haría: ahí el DTO se escribe del lado del cliente. |
| 12 | [**Optimistic updates + LWW con rollback silencioso**](adr/0007-favorites-context.md#4-el-toggle-es-atómico-en-el-dao-y-descarta-el-boolean-que-manda-la-ui) | El usuario no debe esperar a la red para ver el efecto de su acción. El servidor sigue siendo la autoridad; si gana, el cliente se corrige solo. | Complejidad asíncrona real: estados pendientes, reintentos, reconciliación. Y LWW puede perder escrituras concurrentes legítimas — para favoritos es aceptable, para un carrito no lo sería. |

El alcance está por encima del mínimo, y eso fue deliberado: la app que cumple los cuatro requisitos se
sostiene sola, y el resto existe para mostrar cómo se piensa un producto que tiene que crecer.

---

## Índice de ADR

| ADR | Decide |
|---|---|
| [0001](adr/0001-networking-module.md) | Alcance mínimo de `:core:network`: sin envelope de error, sin qualifiers de timeout, sin interceptors de negocio |
| [0002](adr/0002-database-module.md) | Versión de Room, forma de las entities, y la estrategia de tests que costó dos intentos |
| [0003](adr/0003-price-representation.md) | El precio es un `Double` y se formatea en el átomo, no un `Money` sobre `BigDecimal` |
| [0004](adr/0004-catalog-domain-model.md) | Modelo de dominio del catálogo: `Category` como value class, casos de uso como `fun interface` |
| [0005](adr/0005-catalog-data-layer.md) | Capa de datos del catálogo: datasources, upsert con poda transaccional, y el tiempo desde la stdlib |
| [0006](adr/0006-catalog-presentation.md) | Capa de presentación, nacimiento de `:core:connectivity` y Navigation 3 |
| [0007](adr/0007-favorites-context.md) | Contexto de favoritos: tres módulos, y por qué el `enum` nace con un solo caso |
