# Roadmap

Now: the docs and what a real release is missing. Next: user-facing quality. Later: performance,
hardening and the larger designs. Each item links to its issue once one exists.

## Now

- **Docs.** Topic docs, new diagrams (context map, testing by layer, a vertical slice), the README as a
  landing page, influences, and a design-system catalog generated from the screenshot baselines.
- **Continuous delivery.** R8 with keep rules, signing through secrets, a release per tag with the APK
  on GitHub Releases, a changelog, and a `versionCode` derived from the tag or the run.
- **Architecture tests.** Module rules through a module-graph assertion in `check`; code rules with
  Konsist, or ArchUnit or detekt rules if Konsist cannot parse Kotlin 2.3.
- **Instrumented tests** on an emulator in CI: the Room 1 → 2 migration and one navigation journey.
- **Dependency updates.** Dependabot for Gradle and GitHub Actions, grouped, with actions pinned by
  SHA.

## Next

- **API contract.** A scheduled smoke test that decodes the real API into the DTOs.
- **Images.** Decode at display size; one `ImageLoader` over the app's OkHttp client.
- **Accessibility.** Fixed heights that clip text at large font scales, headings and state
  descriptions, a double announcement on the favorite button, and accessibility checks on the
  screenshots.
- **Localization.** English by default with `values-es`; price and rating formatted by locale.
- **State.** Keep the category filter across process death, and restore edge-to-edge.
- **Theme toggle.** Wire the moon slot the top bar already has.
- **Screenshots recorded in CI**, with the diffs on the pull request.
- **Static analysis.** ktlint and detekt with baselines, and Lint with warnings as errors.

## Later

- **Performance.** Baseline Profiles and Macrobenchmark.
- **Observability.** Debug tooling (Timber, StrictMode, LeakCanary), the cause kept in
  `AppError.Unknown`, and crash reporting.
- **Security.** Real backup rules or `allowBackup=false`, and no cleartext traffic.
- **Build hygiene.** Robolectric setup in a convention plugin, the configuration cache in CI, test
  reports on pull requests, `.gitattributes`.
- **Code and docs aligned.** Inject the dispatcher into `executeCall`, and keep `javax.inject` out of
  `:core:common`'s API ([dependency rules](architecture.md#dependency-rules)).
- **Diagrams.** Render the PNGs automatically, and generate the module graph from the build.
- **Own API and server-driven UI** ([proposal](proposals/sdui-and-own-api.md)).
- **Audit context.** An `:audit` context with a durable trail of domain events, as a generic subdomain
  any `data` module may use. It answers *what happened to my data?* once sync reconciles in the
  background.
- **Analytics** designed around this domain's events.
