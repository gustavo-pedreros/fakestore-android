# FakeStore Android

An offline-first product catalog built as a **multi-module DDD + Clean Architecture** Android app.
Kotlin, Compose, Room as the single source of truth, and a module graph where the *build itself*
enforces the architecture.

[![build](https://github.com/gustavo-pedreros/fakestore-android/actions/workflows/build.yml/badge.svg)](https://github.com/gustavo-pedreros/fakestore-android/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/gustavo-pedreros/fakestore-android/branch/main/graph/badge.svg)](https://codecov.io/gh/gustavo-pedreros/fakestore-android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Compose%20BOM-2026.08.00-4285F4?logo=jetpackcompose&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-26-3DDC84?logo=android&logoColor=white)
![modules](https://img.shields.io/badge/modules-14-lightgrey)

---

## Screenshots

<table>
  <tr>
    <td align="center"><img src="art/catalog.png" width="200"><br><sub><b>Catalog</b><br>grid + category filter</sub></td>
    <td align="center"><img src="art/detail.png" width="200"><br><sub><b>Detail</b><br>image, price, category, rating</sub></td>
    <td align="center"><img src="art/favorites.png" width="200"><br><sub><b>Favorites</b><br>survives process death</sub></td>
    <td align="center"><img src="art/offline.png" width="200"><br><sub><b>Offline</b><br>cached data + staleness banner</sub></td>
  </tr>
</table>

---

## What the challenge asked, and where it lives

| # | Requirement | Status | Where |
|---|---|---|---|
| 1 | Product list with image, title and price, plus loading and error states | ✅ | `catalog/ui/catalog/` — four content states, 13 ViewModel tests |
| 2 | Detail screen with large image, title, description, price and category | ✅ | `catalog/ui/detail/` |
| 3 | Favorites that survive a restart | ✅ | `favorites/` — three modules over a Room table |
| 4 | Offline cache with a visual indicator for cached data | ✅ | Room is the SSOT; `FsStatusBanner` shows the age of the data |
| ★ | *"Desirable: build your own API"* | 📋 **Designed, not built** | [Proposal](docs/proposals/sdui-and-own-api.md) |
| ★ | *"If you pull off server-driven UI you're a rockstar"* | 📋 **Designed, not built** | [Proposal](docs/proposals/sdui-and-own-api.md) |

---

## Quick start

Requires **JDK 21** and an **Android SDK** with its licenses accepted, found through `ANDROID_HOME` or
`sdk.dir` in `local.properties`. The wrapper fetches Gradle; missing SDK platforms are downloaded by
the Android Gradle plugin.

```bash
git clone https://github.com/gustavo-pedreros/fakestore-android.git
cd fakestore-android

./gradlew build            # compiles every module, runs the tests, runs lint
./gradlew :app:installDebug
```

Prefer not to build it? Every green CI run publishes the debug APK as a downloadable artifact —
open the [latest run](https://github.com/gustavo-pedreros/fakestore-android/actions/workflows/build.yml)
and grab `fakestore-debug-apk`.

> **The first launch needs a network connection.** There is no mock backend: debug and release both talk
> to `https://fakestoreapi.com/` ([decision 5](docs/decisions.md#5-no-mock-interceptor)).

---

## Architecture

- **DDD draws the boundaries between modules.** Each bounded context, `catalog` and `favorites`, is a
  group of Gradle modules.
- **Clean Architecture orders the layers inside a context:** `ui → domain ← data`.
- **Layers are modules,** so `domain` purity is a classpath property: Retrofit and `android.jar` are not
  on its classpath.

![Module graph: :app wires the catalog and favorites contexts, each with ui, domain and data modules. ui and data depend on their own domain, each ui also depends on the other context's domain, and both contexts use a shared technical core.](docs/diagrams/01-module-graph.png)

Dependency rules, how each is checked, and the module reference: [architecture](docs/architecture.md).

---

## Docs

| Doc | Question it answers |
|---|---|
| [Architecture](docs/architecture.md) | Which modules exist, which dependencies are allowed, and how is each rule checked? |
| [Offline-first](docs/offline-first.md) | Where does the data come from, and what does the user see when the network fails? |
| [Design system](docs/design-system.md) | Where does each piece of UI live, and what did the design settle? |
| [Testing](docs/testing.md) | Which test protects each layer, what gates a merge, and how do screenshots work? |
| [Decisions](docs/decisions.md) | What did each key decision buy, what did it cost, and when would I choose otherwise? |
| [Roadmap](docs/roadmap.md) | What comes now, next and later? |

---

## Stack

| Area | Choice |
|---|---|
| Language / toolchain | Kotlin 2.3.0, JVM 21, `minSdk 26`, `compileSdk 37` |
| UI | Jetpack Compose (BOM 2026.08.00), Material 3, **Navigation 3** |
| Async | Coroutines + Flow — `StateFlow`, `combine`, `flatMapLatest` |
| DI | Hilt 2.60.1 + KSP |
| Network | Retrofit 3.0.0 + OkHttp 5.4.0 + kotlinx.serialization |
| Persistence | Room 2.8.4, schemas exported and version-controlled |
| Images | Coil 3 over OkHttp, stock `ImageLoader` — the disk cache is Coil's default, not a setting here |
| Time | `kotlin.time.Instant` / `Clock` from the stdlib |
| Test | JUnit 5, Turbine, MockWebServer, Robolectric, Roborazzi |
