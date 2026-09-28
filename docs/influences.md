# Influences

Where the ideas in this repo come from, where each one shows in the code, and where I chose
differently. The reasoning behind each departure lives in [decisions](decisions.md) or next to the code.

## Now in Android

[Now in Android](https://github.com/android/nowinandroid), Google's reference Android app.

- **Here:**
  - Build logic in [convention plugins](architecture.md#convention-plugins) and a version catalog.
  - [Test doubles](testing.md#test-doubles) instead of a mocking library, and screenshot tests with
    Roborazzi.
  - Its rule "a feature's `impl` depends only on another feature's `api`" is this repo's "a `ui` uses
    only the other context's `domain`" ([dependency rules](architecture.md#dependency-rules)).
  - Docs the build generates: its module graph in each README, the [UI gallery](ui-gallery.md) here.
- **Where I depart:**
  - Modules group by bounded context, each with `ui`, `domain` and `data`; there, by kind (`core`,
    `feature`).
  - `domain` is mandatory and pure Kotlin/JVM, and ViewModels reach data only through use cases;
    there, ViewModels also read repositories directly.
  - Coverage with Kover and a Codecov ratchet; there, JaCoCo.
  - The module graph is drawn by hand; there, `graphUpdate` generates it.
  - No catalog app for the design system, at least for now: there, `app-nia-catalog` runs every
    component; here, the [UI gallery](ui-gallery.md) shows their screenshots.
  - No benchmarks or Baseline Profiles yet ([roadmap](roadmap.md#later)).

## Google's guide to app architecture

The [guide to app architecture](https://developer.android.com/topic/architecture).

- **Here:** unidirectional data flow with `StateFlow`, UI state held by the ViewModel, repositories,
  and Room as the [single source of truth](offline-first.md#room-as-the-single-source-of-truth).
- **Where I depart:** the guide makes the domain layer optional; here every context has one.

## Clean Architecture

Robert C. Martin, *Clean Architecture* (2017).

- **Here:** the dependency rule, `ui → domain ← data`, enforced by the classpath rather than by review
  ([contexts and layers](architecture.md#contexts-and-layers)). Use cases are each context's boundary,
  and neither Retrofit nor Room reaches a `domain`.
- **Where I depart:** both contexts share one Room database, with separate DAOs
  ([decision 4](decisions.md#4-one-room-database-separate-daos)).

## Domain-Driven Design

Eric Evans, *Domain-Driven Design* (2003).

- **Here:** bounded contexts as module groups ([decision 2](decisions.md#2-a-module-group-is-a-bounded-context)),
  a shared kernel, an anti-corruption layer in two steps (DTO → Room entity → domain), and contexts
  that meet only in presentation ([context map](architecture.md#context-map)). A published language
  (`:shared:contract`) and a generic subdomain (`:audit`) are planned.
- **Where I depart:** the tactical patterns stay light: no aggregates or domain events yet.

## Atomic Design

Brad Frost, [*Atomic Design*](https://atomicdesign.bradfrost.com/) (2016).

- **Here:** atoms, molecules and organisms live in `:core:designsystem`; templates and pages live in each
  feature ([atomic design, translated](design-system.md#atomic-design-translated)). The classpath keeps
  the cut: the design system depends on no `domain`, so an atom cannot import `Product`.
- **Where I depart:**
  - The book offers a mental model; here each layer has a rule that decides where a file goes, because
    Android adds modules, ViewModels and navigation.
  - A template is the stateless `…Screen`; a page is its stateful overload, the only code that touches
    `hiltViewModel()` and navigation. In the book, a page is a template filled with real content.
  - Colors, type, shapes and spacing are theme tokens beneath the atoms, the gallery's [Foundations](ui-gallery.md#foundations);
    Frost's original post counts color palettes and fonts as atoms.

## xUnit Test Patterns

Gerard Meszaros, *xUnit Test Patterns* (2007), the book that named test doubles.

- **Here:**
  - Its taxonomy of doubles, all hand-written ([test doubles](testing.md#test-doubles)):
    - Stubs feed indirect inputs: `FixedClock`, or `refresh = { Either.Error(networkError) }`.
    - Spies record indirect outputs: `onToggleFavorite = { id -> toggledIds.add(id) }`.
    - Fake objects are working in-memory versions: `FakeCatalogLocalDataSource` filters rows like the
      DAO does.
  - State verification: a test asserts what a fake or a spy holds after the call, never expectations
    set up front.
  - Four-phase tests, each with a fresh fixture built by creation methods with defaults
    (`createProductEntity(…)`, `viewModel(…)`).
- **Where I depart:**
  - No mock objects: nothing here needs behavior verification. It is the classicist side of Martin
    Fowler's [*Mocks Aren't Stubs*](https://martinfowler.com/articles/mocksArentStubs.html), which
    builds on this vocabulary.
  - The names do not follow the taxonomy: every double class is a `Fake…`, including stubs such as
    `FakeCatalogRemoteDataSource`.

## Fitness functions

Neal Ford, Rebecca Parsons and Patrick Kua, *Building Evolutionary Architectures* (2017).

- **Here:** checks that fail the build when a property erodes, instead of trusting review:
  - The classpath keeps `domain` pure ([rule 1](architecture.md#dependency-rules)).
  - `PreviewNamingTest` keeps every preview named `Preview<Name>`, so Kover's filter cannot hide
    production code again ([screenshots](testing.md#screenshots)).
  - Roborazzi fails when a screen no longer matches its baseline, and `uiGalleryCheck` when the
    [UI gallery](ui-gallery.md) no longer matches the baselines.
  - Codecov's ratchet fails when coverage drops against the base branch ([coverage](testing.md#coverage)).
  - lychee fails on a broken link between docs, and a line budget keeps the README a landing page.
- **Where I depart:**
  - Dependency rules 2 to 5 still rely on build files or review; architecture tests are on the
    [roadmap](roadmap.md#now).
  - Only structure, pixels and docs are guarded. Runtime properties such as startup time get no
    fitness function until benchmarks arrive ([roadmap](roadmap.md#later)).
