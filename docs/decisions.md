# Decisions

Each entry says what I gained, what I paid, and when I would choose otherwise. A superseded decision
is marked as such, not deleted.

## 1. A layer is a Gradle module

- **Gained.** `domain` purity is a classpath property: importing Retrofit there does not compile, and
  the Android framework is out of reach. Breaking the rule takes a build-file change, visible in the
  diff.
- **Paid.** Many modules for few screens: slower syncs and more Gradle to maintain.
- **Otherwise.** On a small team or an exploratory product, vertical slices with layers as packages.

## 2. A module group is a bounded context

- **Gained.** A context can be understood, tested and replaced whole. Growing adds modules instead of
  editing existing ones.
- **Paid.** Explicit contracts between contexts, and the discipline to resist shortcuts.
- **Otherwise.** An app with one real context gains nothing from the split.

## 3. Contexts integrate in presentation

- **Gained.** Each `ui` uses the other context's domain API, and nothing below `ui` crosses contexts.
  Each `domain` stays independent, and no `data` module couples to another context's tables.
- **Paid.** Each `ui` knows two domains. The rule lives in build files and review until architecture
  tests arrive.
- **Otherwise.** If the contexts had to share behavior below the UI, a published domain API or domain
  events.

## 4. One Room database, separate DAOs

- **Gained.** One SQLite connection and one migration history, which matters on a phone.
- **Paid.** Entities from several contexts live in one technical module. Every DAO is public, so each
  `data` module using only its own DAO is a convention, not a classpath fact.
- **Otherwise.** Contexts with separate owners or lifecycles, where separate databases pay off.

## 5. No mock interceptor

- **Gained.** No debug-only code path and nothing extra in the Hilt graph. The cold start with no cache
  and no network had to be designed for real. Tests use MockWebServer.
- **Paid.** No instant offline demo without a backend.
- **Otherwise.** Against an unstable or private API, a mock earns its keep.

## 6. `Double` for price, formatted at the leaf

- **Gained.** The API sends `price` as a JSON number with no currency, and nothing in the app does
  arithmetic on it, so there is no rounding error to accumulate.
- **Paid.** Nothing yet.
- **Otherwise.** A `Money` type over `BigDecimal` as soon as there is a cart, a total, a tax or a
  currency field.

## 7. The favorite toggle ignores the UI's boolean

- **Gained.** The design system reports the `checked` value the user saw; each screen template drops it
  (`{ id, _ -> onFavoriteToggle(id) }` in `CatalogScreen.kt` and `FavoritesScreen.kt`) and a
  `@Transaction` in `FavoriteDao` reads the table and flips it. Two fast taps cannot desync anything.
- **Paid.** A design-system parameter ignored on purpose, which reads like an oversight.
- **Otherwise.** Against a `PUT /favorites/{id} {favorite: true}` backend, an idempotent
  `setFavorite(id, desired)` handles retries better.

## 8. Navigation 3, with entries owned by each feature

- **Gained.** The back stack is app-owned state with typed `@Serializable` keys. Each feature registers
  its entries, so `:app` never learns which screens a feature has.
- **Paid.** A young library with a smaller ecosystem than Navigation 2.
- **Otherwise.** On a codebase deep in Navigation 2 with fragments, the migration does not pay for
  itself.

## 9. Favorites without a foreign key

- **Gained.** `favorites` stores only a `productId`, so either context can change or disappear without
  breaking integrity on disk.
- **Paid.** The join happens in memory, in each ViewModel.
- **Otherwise.** Once the catalog is paginated, the in-memory join stops being free; that is the
  trigger to revisit, not table size.

## 10. Storage is grouped by model, behind an own port where the concept is small

- **Gained.** `:core:storage` holds one module per data model: Room for rows, `keyvalue` for small
  values. A key-value store is small and universal, so it sits behind a port this repo owns:
  `KeyValueStore`, with typed keys. Its names survive a library change and map to other platforms
  (`UserDefaults` on iOS), and DataStore stays off its consumers' classpath. Each owner opens its own
  store, a file named after it, and keeps its keys private, so two owners cannot collide.
- **Paid.** An own API: five value types and no codecs, so an owner maps its own format (the theme
  stores the enum name). Changing a key's type needs a new name, because a value of another type reads
  as null. Moving Room renamed its package and its schema folder.
- **Otherwise.** Room and Retrofit stay handed over: their API is the value, and a port would copy it.
  With several owners, a schema per store and runtime checks can come without an API change. A second
  adapter, like Now in Android's `sync:work`, would split port and adapter into two modules. With KMP,
  the port moves to `commonMain` as it is.

## 11. The theme is app-shell state, kept in a key-value store

- **Gained.** One owner: `:app` reads the stored mode, applies it and handles the toggle. The catalog
  only hosts the button and gets the mode as a value; nothing depends on `:app`, so no context can
  reach the preference. Each toggle reads and writes in one transaction, so two fast taps cannot lose
  a step (as in decision 7).
- **Paid.** The splash and the window background are XML resources that follow the system theme, so a
  cold start shows the system's colors for an instant when the stored mode differs. A splash-screen
  dependency.
- **Otherwise.** With a minimum SDK of 31, the platform's per-app night mode applies the choice to the
  whole configuration, splash included. With a settings screen, a preferences module that owns its
  store.
