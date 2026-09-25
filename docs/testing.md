# Testing

Every test runs on the JVM inside `./gradlew build`, screenshots included, so Kover measures all of
them. Codecov gates each pull request with a ratchet instead of a fixed target. Still missing:
architecture tests and instrumented tests.

## Strategy by layer

![Tests by layer: JVM unit tests cover ViewModels, use cases, repositories and network wiring; Robolectric covers design-system components, Room DAOs and connectivity callbacks; screenshots cover every screen and design-system preview; instrumented, contract and architecture tests are planned.](diagrams/08-testing-by-layer.png)

| Layer | What is tested | Tooling | Status |
|---|---|---|---|
| Kernel and common | `Either`, error types | JUnit 5 | ✅ |
| Domain | Use cases that carry logic | JUnit 5 | ✅ |
| Data | Repositories against fakes; the remote data source against MockWebServer with real payload shapes; the ACL mappers | JUnit 5, MockWebServer | ✅ |
| Network | OkHttp, Retrofit and JSON wiring; the empty-body converter; the error ACL | JUnit 5, MockWebServer | ✅ |
| Persistence | DAO behavior, including the favorite toggle transaction | Robolectric | ✅ |
| Connectivity | The `ConnectivityManager` callback flow: several networks, captive portals, unregistration | Robolectric, Turbine | ✅ |
| Presentation | Every ViewModel state, with virtual time | JUnit 5, Turbine, `kotlinx-coroutines-test` | ✅ |
| Screens | Every preview of the feature screens, light and dark | Robolectric, Roborazzi | ✅ |
| Design system | Every preview, light and dark | Robolectric, Roborazzi, ComposablePreviewScanner | ✅ |
| Architecture | The [dependency rules](architecture.md#dependency-rules) as tests | — | 📋 [Now](roadmap.md#now) |
| Instrumented | The Room 1 → 2 migration and one navigation journey, on an emulator in CI | — | 📋 [Now](roadmap.md#now) |
| API contract | A scheduled smoke test that decodes the real API | — | 📋 [Next](roadmap.md#next) |
| Accessibility | Accessibility checks on the screenshots | — | 📋 [Next](roadmap.md#next) |

Robolectric and Compose's test rules are JUnit 4; they run through the vintage engine next to JUnit 5.

## Test doubles

- **No mocking library.** Most use cases are `fun interface`s, so a double is a lambda.
  `ObserveCategories` is a class, tested over a fake repository. Fakes with state are small
  hand-written classes.
- **Robolectric only where the framework is under test:** `:core:database`, `:core:connectivity` and
  the screenshot modules. Each `data` module talks to a `LocalDataSource` interface, not a DAO, so its
  tests are plain JVM. Everything downstream of `NetworkMonitor` uses
  [`FakeNetworkMonitor`](../catalog/ui/src/test/kotlin/cl/gus/labs/fakestore/catalog/ui/FakeNetworkMonitor.kt).
- **Dispatchers.** `ConnectivityNetworkMonitor` takes its dispatcher through `@IoDispatcher`, so its tests
  pass an `UnconfinedTestDispatcher` on the `runTest` scheduler and the callback assertions cannot race.
  `executeCall` takes a dispatcher too, but defaults to `Dispatchers.IO` and production code uses the
  default ([`ApiCall.kt`](../core/network/src/main/kotlin/cl/gus/labs/fakestore/core/network/call/ApiCall.kt)).
- **Main dispatcher.** ViewModel tests swap it with
  [`MainDispatcherExtension`](../core/testing/src/main/kotlin/cl/gus/labs/fakestore/core/testing/MainDispatcherExtension.kt).

## Screenshots

`fakestore.android.compose.testing` renders each module's previews on the JVM with Robolectric's
native graphics, and Roborazzi compares them with committed baselines. No emulator is involved, and
Kover sees the run.

- **Where.** `:core:designsystem`, `:catalog:ui` and `:favorites:ui`. Each has a `PreviewScreenshotTest`
  that finds its previews with ComposablePreviewScanner, and baselines under `src/test/screenshots/`.
- **Pinned rendering.** SDK and screen size come from `src/test/resources/robolectric.properties`. The
  test JVM runs in UTC, so dates render the same on every machine.
- **When they compare.** Only with a Roborazzi mode on. A plain `./gradlew build` skips them; CI adds
  `-Proborazzi.test.verify=true` and uploads the `*_actual.png` and `*_compare.png` diffs when one
  fails.
- **No network.** In `:core:designsystem`, `DesignSystemTestApplication` installs a fake Coil engine:
  known test URLs load, break or never finish, and any other URL fails fast. The feature previews use
  no image URL.
- **Naming.** Previews are named `Preview<Name>`, and a `PreviewNamingTest` per module enforces it.
  Kover's `@Preview*` filter matches by name prefix: `ProductCardPreview` used to hide `ProductCard`
  from the report too ([`KoverFilters.kt`](../build-logic/convention/src/main/kotlin/cl/gus/labs/fakestore/convention/KoverFilters.kt)).

```bash
./gradlew :core:designsystem:verifyRoborazziDebug
# After an intended visual change; commit the images with the change.
./gradlew :core:designsystem:recordRoborazziDebug -Proborazzi.cleanupOldScreenshots=true
```

Swap the module for `:catalog:ui` or `:favorites:ui` as needed.

## Coverage

- **One report.** Kover merges the `coverage` variant of every production module:
  `./gradlew :koverXmlReportCoverage`. Generated code, previews and DI wiring are excluded once, in
  `KoverFilters.kt`, and both Kover plugins apply the same list.
- **The gate is Codecov** ([`codecov.yml`](../codecov.yml)): the project total may not drop against the
  base branch, and new code needs its own patch coverage. A ratchet catches a slide that an absolute
  target would allow.
- **By layer.** Codecov components split the same report into the layers of the diagram above.

| Layer | Coverage on `main` |
|---|---|
| domain | ![domain coverage](https://codecov.io/gh/gustavo-pedreros/fakestore-android/branch/main/graph/badge.svg?component=domain) |
| data | ![data coverage](https://codecov.io/gh/gustavo-pedreros/fakestore-android/branch/main/graph/badge.svg?component=data) |
| presentation | ![presentation coverage](https://codecov.io/gh/gustavo-pedreros/fakestore-android/branch/main/graph/badge.svg?component=presentation) |
| design system | ![design system coverage](https://codecov.io/gh/gustavo-pedreros/fakestore-android/branch/main/graph/badge.svg?component=design_system) |
| infrastructure | ![infrastructure coverage](https://codecov.io/gh/gustavo-pedreros/fakestore-android/branch/main/graph/badge.svg?component=infrastructure) |

- **The local floor is not a gate.** `:koverVerifyCoverage` checks a minimum in
  [`KoverRootConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/cl/gus/labs/fakestore/convention/KoverRootConventionPlugin.kt),
  but neither `check` nor CI runs it.
- **Finding gaps.** Line coverage hides an untaken `when` branch, and a class the tests only construct
  can reach 100% with no assertion. The per-method `BRANCH` counters of the Kover XML find those; a
  deliberate mutation of the production line confirms that a named test fails. Some missed branches
  are unreachable, such as the `label` dispatch of a `suspend` function.

## CI

[`build.yml`](../.github/workflows/build.yml) runs on pushes to `main` and `develop` and on pull
requests to both.

| Change | What runs |
|---|---|
| Code | `./gradlew build :koverXmlReportCoverage -Proborazzi.test.verify=true`, the Codecov upload and the debug APK artifact |
| Docs only (`docs/`, `art/`, `*.md`, `LICENSE`) | No Gradle. An empty Codecov upload, which passes because every changed file is ignored |
| Any | lychee checks the links and anchors between Markdown files, offline |

Runs on `main` and `develop` are Codecov's base reports: they are never cancelled, and both branches
write the Gradle cache.
