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
