# ADR-0002: Alcance y decisiones técnicas de `:core:database`

## Estado

Aceptada — 2026-08-22 · **Corregida — 2026-08-23** (la Decisión 5 quedó desmentida por la implementación; ver
[Corrección](#corrección--2026-08-23)) · **Decisión 2 parcialmente superada — 2026-08-23** por
[ADR-0003](0003-price-representation.md): `price` pasó de `String` a `Double`. Todo lo demás se mantiene. ·
**Decisión 2 corregida de nuevo — 2026-08-24** al construir `:favorites:data` (ver
[Corrección](#corrección--2026-08-24)): `syncState` nació con un solo caso, no tres, y `kotlinx-datetime`
nunca entró.

## Contexto

`:shared:kernel`, `:core:common` y `:core:network` están cerrados y mergeados (PR #1, PR #2). Tocaba elegir
el siguiente bloque de Etapa 1; se preguntó entre `:catalog:domain`, `build-logic/` y `:core:database`, y se
eligió **`:core:database`** — el corazón del requisito de offline y de la decisión "Room como fuente única de
verdad" (ver `ARCHITECTURE.md`), asumiendo un riesgo señalado de antemano: sin `:catalog:domain` todavía, las entities se
diseñan sin un modelo de dominio asentado que mapear.

Ese riesgo se mitiga así: las entities no inventan forma, se derivan de dos fuentes ya firmes —

1. **La forma real de `fakestoreapi.com`**, ya sondeada por `curl` en la sesión de ADR-0001 (`id`, `title`,
   `price`, `description`, `category`, `image`, `rating.rate`, `rating.count`).
2. **Los seams ya decididos por adelantado**: la tabla `favorites` nace con `updatedAt` y `syncState`
   aunque en Etapa 1 siempre valgan `LOCAL_ONLY`, y el banner de datos cacheados se alimenta de
   `sync_metadata.lastSyncedAt`.

**Sin precedente interno que copiar**: es la primera pieza del proyecto donde no hay una convención previa
de la que heredar versión ni patrón. Todo lo que se fija acá —la versión de Room, el mecanismo de
`schemaLocation`, la estrategia de tests— se verificó contra la documentación oficial y las notas de
release, y queda registrado en este documento precisamente porque no había un precedente al que apuntar.

## Decisión

### 1. Versión de Room: 2.8.4 (estable), no la nueva línea 3.0/KMP

El stack ya fijaba "Room 2.8.x (KSP)". Confirmado contra las notas de release: **2.8.4** (19-nov-2025) es la última
estable de la línea `androidx.room` 2.x, que a partir de esa versión entra en modo mantenimiento. Existe una
**Room 3.0** (`androidx.room3`, alpha desde 11-mar-2026, KMP-first, coordenadas nuevas) — se descarta por ser
alpha; no corresponde pinear una librería de persistencia a una versión no estable.

- KSP, no kapt — coherente con el resto del proyecto (KSP 2.3.9 ya está en el catálogo).
- `schemaLocation` vía `ksp { arg("room.schemaLocation", ...) }` (no el nuevo Gradle plugin
  `androidx.room3`, que es de la línea 3.0 que se está evitando). Deja resuelto el seam de migraciones
  aunque en v1 no haya ninguna que escribir — es el seam, no la migración.
- Se agrega `kotlinx-datetime` al catálogo (ya listado en el stack, pero nunca declarado como
  dependencia real) — lo necesita `FavoriteEntity.updatedAt` y `SyncMetadataEntity.lastSyncedAt`.

### 2. Entities

```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val price: String,       // literal crudo, no Double — ver razón abajo (⚠️ superada, ADR-0003)
    val description: String,
    val category: String,
    val imageUrl: String,
    val ratingRate: Double,
    val ratingCount: Int,
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val productId: Int,
    val updatedAt: Instant,           // kotlinx.datetime
    val syncState: SyncState = SyncState.LOCAL_ONLY,   // LOCAL_ONLY | PENDING | SYNCED — enum completo ya (Etapa 2 lo usa)
)

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val scope: String,    // "catalog" — fila única por ahora, sin generalizar (YAGNI)
    val lastSyncedAt: Instant?,
)
```

**`price: String`, no `Double`** — ⚠️ **SUPERADA por [ADR-0003](0003-price-representation.md)**

> El argumento original se conserva íntegro abajo porque es el que había que refutar. Lo que quedó en el
> código es `val price: Double` y columna `REAL`. La premisa que falla es la primera: no era una decisión
> tomada para este proyecto, era una convención importada sin rejustificar, y la API de FakeStore manda
> `price` como número JSON — no hay literal string que preservar, y ninguna etapa hace aritmética con el precio.
> El texto además costaba un `ORDER BY price` lexicográfico, o sea incorrecto.

El diseño vigente entonces establecía que `Money` nunca ve punto flotante: lee el literal crudo del JSON.
Cachear como `Double` en Room perdería esa precisión de forma permanente (un `Double` ya redondeado no se
puede "des-redondear"). Guardar el literal como texto preserva esa decisión hasta que `:catalog:data` lo
parsee a `BigDecimal`/`Money` en el mapper ACL entity→dominio.

**`syncState` con las 3 variantes ya** (no solo `LOCAL_ONLY`): incluir `PENDING`/`SYNCED` ahora es gratis (un
enum) y evita la migración de Room ya identificada como el costo a evitar. Usarlas es
trabajo de Etapa 2 (`FavoritesSyncWorker`); *definirlas* es barato hoy.

**`sync_metadata` con `scope: String` en vez de una tabla singleton implícita**: hoy solo existe `"catalog"`,
pero una PK de texto en vez de asumir "una sola fila siempre" cuesta cero extra y dice explícitamente qué
está fresco, en vez de un booleano tácito. No se generaliza más allá de eso — ningún otro contexto lo
necesita todavía.

### 3. DAOs — `Flow` para lectura, `suspend` para escritura (Room como SSOT)

- `ProductDao`: `observeAll(): Flow<List<ProductEntity>>`, `observeById(id): Flow<ProductEntity?>`,
  `upsertAll(products: List<ProductEntity>)`.
- `FavoriteDao`: `observeIds(): Flow<List<Int>>`, `upsert(favorite: FavoriteEntity)`,
  `deleteByProductId(id: Int)`.
- `SyncMetadataDao`: `observe(scope: String): Flow<SyncMetadataEntity?>`, `upsert(entity: SyncMetadataEntity)`.

### 4. `FakeStoreDatabase` + Hilt — mismo patrón que `:core:network`

`di/DatabaseModule.kt` provee `FakeStoreDatabase` (vía `Room.databaseBuilder`, aplicando `Context` de
`@ApplicationContext`) y cada DAO como binding `@Singleton` — mismo shape que `NetworkModule.kt` en
`:core:network` (factories + módulo Hilt separado, sin qualifiers preventivos).

### 5. Tests: driver JVM embebido de Room, no Robolectric — ⚠️ SUPERADA

> **Esta sección es incorrecta.** Se conserva tal cual se escribió porque el error es instructivo. Lo que
> realmente se construyó está en [Corrección](#corrección--2026-08-23), más abajo.

Confirmado por búsqueda: `Room.inMemoryDatabaseBuilder` con Robolectric tiene problemas documentados y
conocidos con bases in-memory (issue abierto en el propio repo de Robolectric). Room 2.8.x, al ser
KMP-capaz, expone un driver SQLite embebido puro-JVM (`androidx.sqlite:sqlite-bundled`) pensado exactamente
para este caso: tests locales JUnit sin `Context` de Android y sin Robolectric. Se usa ese, agregado como
`testImplementation`. Cumple el criterio de "Room in-memory" en la definición de terminado, sin
arrastrar Robolectric, que este proyecto no usa en ningún otro módulo.

## Corrección — 2026-08-23

Al implementarla, la Decisión 5 falló dos veces seguidas.
Ambos fallos tienen **la misma causa raíz**, que la búsqueda web no reveló porque la documentación de Room
la da por sabida.

### Causa raíz: un módulo `android.library` resuelve variantes Android incluso para sus tests locales

Los artefactos AndroidX con soporte KMP (`room-runtime`, `androidx.sqlite`) se publican en **variantes**:
una JVM/desktop y una Android. Cuál se elige la decide Gradle por *atributos del módulo consumidor*, no por
dónde se ejecuta el código. En un módulo `com.android.library`, la configuración `testImplementation`
resuelve la **variante Android** — aunque los tests corran en la JVM del host, sin emulador. De ahí:

1. **Error de compilación.** `Room.inMemoryDatabaseBuilder<T>()` (reified, sin `Context`) vive en la
   superficie común/KMP. La variante Android expone únicamente
   `Room.inMemoryDatabaseBuilder(context: Context, klass: Class<T>)`. El compilador reportaba
   `No value passed for parameter 'context'` / `'klass'`.
2. **Error en runtime.** Resuelto (1), los 4 tests morían con
   `UnsatisfiedLinkError: no sqliteJni in java.library.path`. La variante **JVM** de
   `androidx.sqlite:sqlite-bundled` trae su binario nativo como recurso del jar y lo auto-extrae; la
   variante **Android** lo trae como `.so` dentro del AAR, esperando que el build lo empaquete en un APK
   instalado y que `System.loadLibrary` lo encuentre en el directorio nativo de la app. En un unit test
   local no hay APK instalado. **Ningún mock de `Context` arregla esto** — el problema es la carga de la
   librería nativa, no el `Context`.

Se intentó primero el camino de MockK (`mockk<Context>(relaxed = true)`) para sortear (1); compiló y
falló en (2). MockK y `sqlite-bundled` se removieron por completo del catálogo — la corrección los deja sin
uso.

### Lo que realmente se construyó

**Robolectric 4.16.1 + el driver por defecto de Room + base sobre archivo + runner JUnit4 vía Vintage.**

| Pieza | Por qué |
|---|---|
| **Robolectric** | Aporta un `Context` real (`RuntimeEnvironment.getApplication()`) y *shadows* de `android.database.sqlite.*` respaldados por un SQLite nativo que él mismo gestiona. Es la pieza que hace ejecutable un módulo Android en la JVM. |
| **Driver por defecto** (sin `.setDriver(...)`) | El driver por defecto de Room en Android pasa por el SQLite del framework — justo lo que Robolectric shadowea. Bonus: es **el mismo driver que usa `DatabaseModule` en producción**, así que el test ejerce el camino real en vez de uno paralelo. `BundledSQLiteDriver` esquiva el framework, así que Robolectric **no** lo arregla: son mecanismos independientes. |
| **Base sobre archivo, no in-memory** | Nombre único por instancia + `deleteDatabase()` en `@After`. Evita [robolectric#8289](https://github.com/robolectric/robolectric/issues/8289) (bases Room in-memory bajo Robolectric). El aislamiento sale igual de bien y cuesta dos líneas. |
| **JUnit4 nativo + `junit-vintage-engine`** | `RobolectricTestRunner` es un `org.junit.runner.Runner`: API JUnit4. `@RunWith` no existe en JUnit5 y Robolectric no tiene soporte JUnit5 de primera parte. Vintage ejecuta esos tests dentro de la misma JUnit Platform que ya usa el resto del proyecto, así que un solo `./gradlew test` corre Jupiter y Vintage juntos. |

Se evaluó y **se descartó** el bridge comunitario `tech.apter.junit5.jupiter:robolectric-extension` (que
permitiría quedarse íntegramente en JUnit5): sus versiones publicadas son 0.1.0/0.5.2, no es oficial de
Robolectric, y meter una dependencia pre-1.0 no oficial en la infraestructura de tests de este proyecto
paga más riesgo del que ahorra. Vintage es parte de JUnit 5 mismo y ya venía gestionado por el `junit-bom`.

Detalle de implementación no obvio: `junit-vintage-engine` va como `testRuntimeOnly`, así que arrastra
`junit:junit:4.13.2` **solo al classpath de runtime**. Como el código de test *importa* `org.junit.Test`,
`@RunWith`, etc., hay que declarar `junit:junit` explícitamente como `testImplementation`. No basta con
heredarlo transitivamente de Robolectric.

### Alcance realmente ejecutado vs. la Decisión 2

`:core:database` se cerró **solo con productos**. `FavoriteEntity`, `SyncState`, `SyncMetadataEntity` y sus
DAOs quedaron diferidos hasta que existiera un consumidor real.
Consecuencia directa: **`kotlinx-datetime` nunca entró al catálogo** y no hay `converter/` — los
`TypeConverter`s de la Decisión 1 y de las Notas de implementación solo los necesitaban las entities diferidas.
`ProductEntity` usa exclusivamente tipos que Room soporta de forma nativa. La versión de Room (2.8.4, Decisión 1) y
la forma de `ProductEntity` (Decisión 2) se sostienen sin cambios.

### Lección para el resto del proyecto

Cuando una dependencia se publica en variantes KMP, **el shape del módulo consumidor decide qué API se ve y
qué binario se baja** — no dónde corre el código. Un `android.library` es Android para todo efecto, incluida
su carpeta `test/`. La guía "usá el driver bundled y olvidate de Robolectric" es real, pero aplica a módulos
`kotlin("jvm")` / KMP puros, no a este.

## Corrección — 2026-08-24

Al construir `:favorites:data` ([ADR-0007](0007-favorites-context.md)), dos predicciones de la Decisión 2 y de la
sección "Alcance realmente ejecutado" no se cumplieron.

**`syncState` nació con un solo caso, no tres.** La Decisión 2 proponía `LOCAL_ONLY | PENDING | SYNCED` completo desde
el principio ("usarlas es trabajo de Etapa 2; *definirlas* es barato hoy"). Al escribir el ADR de favoritos
se revisó ese argumento y no se sostuvo: un enum de un solo caso sigue evitando la migración de Room —lo que
evita la migración es que **la columna** exista, no que el enum tenga sus tres casos— y declarar hoy
`PENDING`/`SYNCED` habría sido código muerto —dos casos sin ningún consumidor hasta la Etapa 2— del tipo
que obliga a dar explicaciones en cada revisión. Ver ADR-0007 §3 para el argumento completo.

**`kotlinx-datetime` no volvió a evaluarse — se descartó directamente.** La Decisión 1 y la fila de "Dependencias" abajo
dejaban la puerta abierta a reconsiderarlo con `:favorites:data`. `FavoriteEntity.updatedAt` terminó siendo
`Long` (epoch millis UTC), igual que `SyncMetadataEntity.lastSyncedAt` ya lo era: Room no necesita un
`TypeConverter` para long, y el resto del proyecto ya usa `kotlin.time.Instant`/`Clock` (stdlib, ver ADR-0005
§D3) en las capas que sí hablan de tiempo. `kotlinx-datetime` habría sido una segunda librería de tiempo
compitiendo con la que ya está en uso, sin que ningún caso de uso la pidiera.

Ninguna de las dos correcciones tocó la versión de Room (Decisión 1) ni la forma de `ProductEntity` (Decisión 2): siguen
firmes.

## Consecuencias

- `:core:database` es la primera decisión técnica del proyecto sin precedente interno que copiar — las
  versiones se fijaron por verificación contra la fuente oficial, no por herencia. Si aparece otra librería
  sin precedente más adelante, este documento es el ejemplo de cómo dejar constancia de esa verificación.
- Diseñar las entities antes de que exista `:catalog:domain` es un riesgo aceptado conscientemente, no
  ignorado: están ancladas a la forma real de la API y a seams ya decididos, no a
  suposiciones nuevas. Si el modelo de dominio termina divergiendo, el costo lo absorbe el mapper ACL de
  `:catalog:data` (que ya estaba previsto como la capa que traduce), no una reescritura de estas entities.
- ~~`price: String` difiere el parseo a `BigDecimal`/`Money` hasta `:catalog:data` — hay que recordar
  escribir ese parseo ahí…~~ **Superado** por [ADR-0003](0003-price-representation.md): `price` es `Double`
  en toda la cadena, así que no hay parseo que recordar ni conversión que escribir en el ACL. La consecuencia
  que queda es la inversa: se pierde la seguridad de tipos que un value object daba, a cambio de borrar el
  serializer custom, el `TypeConverter` y el mapeo.
- ~~`syncState` con sus 3 variantes queda sin uso real hasta Etapa 2…~~ **Corregido**: nació con un solo
  caso (`LOCAL_ONLY`) — ver [Corrección — 2026-08-24](#corrección--2026-08-24). `PENDING`/`SYNCED` se agregan
  cuando `FavoritesSyncWorker` los necesite; hasta entonces no existen en el código.
- ~~El driver JVM embebido de Room (sin Robolectric) es un patrón nuevo en este repo…~~ **Corregido**:
  el precedente que queda es el opuesto — Robolectric + driver por defecto + base sobre archivo, con runner
  JUnit4 puenteado por Vintage. Es el primer y único punto del repo donde se usa JUnit4; el resto sigue en
  Jupiter y ambos conviven en la misma JUnit Platform. Si otro módulo Android necesita tests de Room, este
  es el patrón a copiar.
- Este ADR pasa a ser también el precedente de **cómo se enmienda un ADR** en este repo: la decisión errada
  se conserva marcada como superada y la corrección se agrega como sección propia, en vez de reescribir el
  documento. Un ADR registra por qué se decidió algo *en su momento*; borrar el error borra el aprendizaje.

## Dependencias

| Librería | Versión | Motivo |
|---|---|---|
| Room (runtime, ktx) | 2.8.4 | Última estable de la línea 2.x; 3.0/KMP está en alpha (11-mar-2026) |
| Room compiler | 2.8.4 | Vía KSP, no kapt |
| ~~kotlinx-datetime~~ | — | **No entró, y no volvió a evaluarse.** `FavoriteEntity.updatedAt` terminó en `Long` (epoch millis), igual que `SyncMetadataEntity.lastSyncedAt` — ver [Corrección — 2026-08-24](#corrección--2026-08-24) |
| ~~androidx.sqlite:sqlite-bundled~~ | — | **No entró.** Su variante Android no carga el nativo en tests locales — ver [Corrección](#corrección--2026-08-23) |
| Robolectric | 4.16.1 | Sandbox Android en la JVM: `Context` real + shadows de `android.database.sqlite` |
| junit:junit | 4.13.2 | `RobolectricTestRunner` es JUnit4; importado por el código de test, así que va explícito |
| junit-vintage-engine | *(gestionado por `junit-bom` 5.14.4)* | Ejecuta los tests JUnit4 dentro de la JUnit Platform que ya usa el resto del proyecto |

## Notas de implementación

> Previsión al momento de escribir el ADR. Lo realmente construido difiere: solo productos, sin
> `converter/`, y con `fakestore.android.room` (convention plugin, que no existía cuando se escribió esto)
> en vez de configurar KSP a mano. Ver la [Corrección](#corrección--2026-08-23).

```
core/database/           android.library + hilt + ksp + android.junit5
                          entity/{ProductEntity,FavoriteEntity,SyncMetadataEntity}.kt
                          entity/SyncState.kt (enum)
                          dao/{ProductDao,FavoriteDao,SyncMetadataDao}.kt
                          FakeStoreDatabase.kt
                          converter/{InstantConverter,SyncStateConverter}.kt (TypeConverters)
                          di/DatabaseModule.kt
                          tests: DAO round-trip contra Room.inMemoryDatabaseBuilder + sqlite-bundled
settings.gradle.kts       include(":core:database")
gradle/libs.versions.toml  room=2.8.4, kotlinxDatetime (versión a confirmar en el paso), sqlite-bundled
```

Explícitamente fuera de este paso:
- Cualquier mapper entity→dominio (`:catalog:data`, que no existe todavía).
- Migraciones reales (v1 es la primera versión; `schemaLocation` es el seam, no hay `Migration` que
  escribir).
