# ADR-0001: Alcance mínimo del módulo de networking

## Estado

Aceptada — 2026-08-21. Corregida tras la implementación (ver [Decisión 2](#2-apperror-conserva-la-forma-se-descarta-errorcodeapi)).

## Contexto

Toca construir el primer bloque de infraestructura de red de la Etapa 1 (`:shared:kernel` +
`:core:network`). El diseño por defecto para una capa de red de este tipo asume tres cosas — un envelope de
error estructurado, varios perfiles de timeout, varios interceptors de negocio — y ninguna es gratis. Antes
de escribir el módulo valía la pena confirmar contra la API real cuáles de esas suposiciones aplican acá. Un
sondeo en vivo de `fakestoreapi.com` (vía `curl`) responde con evidencia concreta:

| Caso | HTTP | Body |
|---|---|---|
| `GET /products/1` | 200 | JSON del producto |
| `GET /products/abc` (id no numérico) | **200** | **vacío** |
| `GET /products/99999` (id inexistente) | **200** | **vacío** |
| `GET /nonexistent` (ruta inexistente) | 404 | página HTML de Express, no JSON |
| `POST /auth/login` credenciales inválidas | 401 | texto plano `"username or password is incorrect"` |
| `POST /auth/login` body malformado | 400 | página HTML de Express |
| `POST /products` body `{}` | 201 | acepta cualquier cosa |

No existe en ningún caso un envelope JSON de error: los fallos llegan como HTML de Express o como texto
plano, nunca como un objeto con código y mensaje. Y el caso más importante: **un id de producto inválido o
inexistente no da 404 — da 200 con body vacío.** No es que los errores sean simples y solo de bad request;
es que casi no hay contrato de error que modelar.

## Decisión

### 1. `:shared:kernel` se crea ahora, pero no por el motivo de networking

La necesidad de `:shared:kernel` no depende de qué tan simple sea la capa de red: existe porque `ProductId`
va a ser hablado tanto por `:catalog:domain` como por `:favorites:domain` (favoritos se guarda por id de
producto) — el criterio de `ARCHITECTURE.md` para `:shared:kernel` ("solo entra lo que dos o más contextos
hablan de verdad"). Ese cruce existe sin importar el modelo de errores.

Lo que sí cambia por el modelo de errores es qué entra al módulo *ahora*: solo `AppError` ([Decisión 2](#2-apperror-conserva-la-forma-se-descarta-errorcodeapi)).
`ProductId`/~~`Money`~~/`Category`/`Rating` nacen cuando exista `:catalog:domain` — no se anticipan vacíos
en este paso. (`Money` terminó no escribiéndose nunca: ver [ADR-0003](0003-price-representation.md).)

- **Gano**: el seam ya existe cuando `:catalog:domain`/`:favorites:domain` lo necesiten — cero costo de
  mover `AppError` de paquete más adelante.
- **Pago**: un módulo casi vacío hoy (un solo archivo). Aceptable porque es `kotlin("jvm")` puro — mismo
  costo de plugin que ya se pagó con `:core:common`.
- **Cuándo no lo haría**: un prototipo desechable de una sola pantalla — ahí `AppError` iría directo en
  `:core:common`.

### 2. `AppError` conserva la forma, se descarta `ErrorCode`/`Api`

Sin envelope de error que decodificar, un `ErrorDto.kt` + una rama `AppError.Api(code: ErrorCode, ...)` no
tienen nada real que mapear — sería una taxonomía de un solo valor (`UNKNOWN`). Se recorta a:

```kotlin
sealed interface AppError {
    data class Http(val httpStatus: Int, val rawBody: String?) : AppError
    data class Network(val message: String?) : AppError
    data object EmptyBody : AppError
    data class Unknown(val message: String?) : AppError
}
```

`EmptyBody` deja de ser un caso decorativo: por el hallazgo de la tabla de arriba, es la señal real de
"producto no encontrado" en esta API (200 + body vacío), no un 404. Es una excentricidad de la API, no una
decisión de diseño, y hay que dejarla documentada para que no parezca un bug futuro. Va acá y no en un
comentario: el código de este repo no lleva comentarios, así que este ADR es el lugar donde se busca por qué
existe `EmptyBody`.

- **Pago diferido**: cuando exista el servidor propio de la Etapa 2, si ahí sí se definen códigos de error
  propios, `AppError.Api` + `ErrorCode` vuelven — es el "sistema más elaborado" que ya se anticipaba.

**Corrección — 2026-08-21.** La primera implementación no hacía verdad esta afirmación: Retrofit solo
devuelve `body() == null` gratis para 204/205, no para un 200 con `Content-Length: 0` (que es lo que envía
esta API). Con el converter de kotlinx.serialization sin envolver, ese caso intentaba parsear un string vacío
como JSON, lanzaba `SerializationException`, y terminaba mapeado a `AppError.Unknown` — no a `EmptyBody`.
Confirmado con un test de integración contra un `MockWebServer` real (`ExecuteCallIntegrationTest`, no con
`Response` fabricados a mano, que no ejercitan el converter). Arreglado con un `Converter.Factory` propio
(`EmptyBodyAwareConverterFactory`) que envuelve al de kotlinx.serialization y devuelve `null` cuando
`contentLength() == 0L`, antes de que el converter delegado intente decodificar. Limitación conocida y
aceptada: depende de que el servidor declare `Content-Length`; no cubre un body vacío servido con
transfer-encoding chunked (`contentLength() == -1`) — no hay evidencia de que esta API lo haga.

### 3. No se crean qualifiers de timeout (`BigTimeout`/`NormalTimeout`)

Un enum de timeouts con `NORMAL`/`BIG` y su `@Qualifier` es un patrón habitual, y su costo real no es
declararlo sino mantener dos ramas del grafo de Hilt vivas. Se paga cuando hay endpoints con perfiles de
latencia distintos: una descarga pesada junto a un GET de catálogo.

Acá hay un solo `baseUrl` y una API pública uniforme — todos los endpoints de la Etapa 1 tienen el mismo
perfil. Un segundo timeout no tendría a quién servir, y un qualifier sin dos consumidores es ceremonia. Se
define una sola constante (15s), sin enum ni qualifier.

- **Cuándo revisar esto**: si en Etapa 2 el servidor propio tiene un endpoint con perfil de latencia distinto
  (ej. una carga), ese es el momento de introducir el qualifier — no antes.

### 4. Interceptors: solo logging (debug)

| Interceptor considerado | ¿Se incluye? | Por qué |
|---|---|---|
| Headers por defecto (canal, request-id) | No | Sirven a un backend con trazabilidad propia; fakestoreapi.com no pide ningún header |
| Identificación de cliente | No | Etapa 1 no tiene endpoints autenticados por cliente (el catálogo es público) |
| Clave de idempotencia | No | Etapa 1 es de solo lectura (GET catálogo/detalle); no hay POST/PUT propios |
| `HttpLoggingInterceptor` (solo debug) | **Sí** | Barato, estándar, útil en depuración |
| Mock desde `assets/` | No | Descartado por decisión propia: ver `ARCHITECTURE.md`, "sin mock interceptor" |

Efecto neto: `OkHttpClientFactory` recibe una lista de interceptors casi vacía — solo logging en debug.

## Consecuencias

- `:core:network` nace deliberadamente chico: sin `ErrorDto`, sin `ErrorCode`, sin qualifiers de timeout,
  sin interceptors de negocio. Menos superficie que explicar y que mantener, y coherente con el mismo
  criterio que descartó el mock interceptor.
- `EmptyBody` como señal de "no encontrado" es un acoplamiento implícito al comportamiento actual de
  `fakestoreapi.com`. Si la API cambiara a devolver 404 real, el mapeo de errores tendría que revisarse — es
  un riesgo aceptado porque la API es pública, estable y no versionada por este proyecto.
- Si Etapa 2 trae servidor propio, se espera *ampliar* este ADR (o escribir uno nuevo) en vez de reescribirlo: los
  campos que hoy se omiten (`Api`, `ErrorCode`, qualifiers de timeout) tienen un lugar claro adonde volver.

## Dependencias (BOM)

> Versiones al 2026-08-21, cuando se cerró el módulo. La fuente viva es `gradle/libs.versions.toml`;
> este cuadro no se actualiza.

| Librería | Versión (BOM) |
|---|---|
| Retrofit | 3.0.0 |
| OkHttp | 5.4.0 |
| kotlinx.serialization | 1.10.0 |
| kotlinx.coroutines | 1.10.1 |
| Hilt | 2.59.2 |
| KSP | 2.3.9 |

No se agregan `mockwebserver` ni `mockk` en este paso — nada en `:core:network` los usa todavía; se agregan
cuando `:catalog:data` los necesite, mismo criterio de "solo lo que se usa" ya aplicado al trimming anterior
del version catalog.

## Notas de implementación

Lo que efectivamente se construyó. Algunos puntos cambiaron respecto al diseño original de esta sección: una
auditoría posterior a la implementación encontró código sin consumidor real, y se recortó.

```
shared/kernel/          kotlin("jvm") — AppError.kt (4 variantes, arriba)
core/network/            android.library + kotlin.serialization + hilt + ksp + android.junit5
                          config/{NetworkConfig,NetworkJson,OkHttpClientFactory,RetrofitFactory}.kt
                          config/EmptyBodyAwareConverterFactory.kt (ver Corrección arriba)
                          client/ApiServiceFactory.kt (un solo create(Class<T>) — sin el reified
                            create<T>() ni ApiClient/RetrofitApiClient: un mecanismo paralelo que
                            ningún consumidor llegaba a usar)
                          call/ApiCall.kt (executeCall — sin executeEmptyCall, Etapa 1 es solo lectura)
                          error/ErrorMapper.kt (recortado, sin ErrorDto)
                          di/NetworkModule.kt (sin NetworkQualifiers.kt — @BaseUrl no tenía con qué
                            colisionar; baseUrl viaja como String sin qualificar)
                          tests: ApiCallTest.kt, ErrorMapperTest.kt, ExecuteCallIntegrationTest.kt
                            (MockWebServer real — es la que probó la Corrección de arriba)
```

También se corrigió, fuera del alcance original de este ADR:
- `:core:common` y `:shared:kernel` compilaban a bytecode JVM 21 (`jvmToolchain`) mientras `:app` y
  `:core:network` declaraban `compileOptions` en Java 11 — Kotlin no permite inlinear bytecode JVM21 en un
  target JVM11, lo que habría roto el primer `.map {}` sobre `Either` desde `:catalog:data`. Ambos módulos
  quedaron alineados a 21.

Explícitamente fuera de este paso (sin cambios respecto al plan original):
- `NetworkMonitor` (necesita `Context`/`ConnectivityManager`; su consumidor real es el ViewModel de catálogo) — se construye junto a `:catalog:data`/`:catalog:ui`.
- `ProductsApi` y sus DTOs — son de `:catalog:data`, que no existe todavía.
- Bootstrap de Hilt en `:app` (`FakeStoreApp`, `android:name` en el manifest) — todavía no se hizo.
