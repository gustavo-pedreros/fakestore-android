# Proposal: remote config owned by each context

Designed, not built. Remote values reach the app through typed keys. Each context declares its own keys,
and no central catalog exists. Screens never see a key or a vendor: they see a domain decision, like any
other use case.

## A key carries its type and its default

A key is the only place that knows the parameter's name, its type and the value to use when the remote one
is missing. Parsing belongs to the type, and parsing never throws:

```kotlin
enum class Activation { NextLaunch, Immediate }

class ConfigKey<T : Any> private constructor(
    val key: String,
    val default: T,
    val type: ConfigType<T>,
    val activation: Activation,
) {
    companion object {
        fun bool(key: String, default: Boolean, activation: Activation = Activation.NextLaunch) =
            ConfigKey(key, default, ConfigType.Bool, activation)

        fun <T : Any> json(
            key: String,
            default: T,
            serializer: KSerializer<T>,
            activation: Activation = Activation.NextLaunch,
        ) = ConfigKey(key, default, ConfigType.Json(serializer), activation)
    }
}

sealed interface ConfigType<T : Any> {
    fun parse(raw: String): T?
    fun encode(value: T): String
}
```

`ConfigType` has four cases: `Bool`, `Text`, `Whole` and `Json(serializer)`. `Whole` accepts `"8"` and
`"8.0"`, and `Bool` accepts `true`, `false`, `1` and `0`. Anything else parses to `null`.

## One read, three layers

![Resolution ladder: a debug override first, then the remote value (the session snapshot for next-launch keys, the latest fetch for immediate keys), and the key's default as the floor. A value that does not parse counts as absent.](diagrams/remote-config-resolution.png)

```kotlin
interface RemoteConfig {
    operator fun <T : Any> get(key: ConfigKey<T>): T
    val activations: Flow<Unit>
}

class LayeredRemoteConfig(
    private val overrides: RawValues,
    private val session: RawValues,
    private val latest: RawValues,
    override val activations: Flow<Unit>,
) : RemoteConfig {
    override fun <T : Any> get(key: ConfigKey<T>): T {
        val remote = if (key.activation == Activation.Immediate) latest else session
        return sequenceOf(overrides, remote)
            .firstNotNullOfOrNull { layer -> layer.raw(key.key)?.let(key.type::parse) }
            ?: key.default
    }
}
```

Every backend reduces to `RawValues`, a lookup from key to string. The type is applied in one place, so a
debug build and a release build cannot disagree about what `"5"` means.

## A fetch lands now and counts next launch

![Cold start: the runtime promotes the pending values to active in DataStore and releases the splash from that local snapshot; the fetch then stores new values as pending for the next launch, and only immediate keys follow it within the session.](diagrams/remote-config-lifecycle.png)

- **Cold start.** The runtime reads its DataStore snapshot and promotes `pending` to `active`. That snapshot
  is the session. The splash already waits for the theme
  ([decision 10](../decisions.md#10-the-theme-is-app-shell-state-kept-in-datastore)), and it also waits for this
  local read. It never waits for the network.
- **Fetch.** New values are stored as `pending`. A screen does not change under the user's finger.
- **Immediate keys.** Ops toggles, such as turning off a misbehaving retry, follow the latest fetch through
  `observe(key)` without a restart.
- **Failures.** A failed fetch, a corrupted file or a malformed value never blocks: the read falls to the
  next layer.

## Modules

![Module graph: the app depends on a bundle that picks the value source, JSON over HTTP now and Firebase in stage D; the source and the runtime depend on the pure-Kotlin API, which is the only remote config module the contexts' data modules see; a debug-only module adds overrides.](diagrams/remote-config-modules.png)

| Module | Type | Responsibility |
|---|---|---|
| `:remoteconfig:api` | Kotlin/JVM | `ConfigKey`, `ConfigType`, `RemoteConfig`, `LayeredRemoteConfig`, `RemoteSource` |
| `:remoteconfig:runtime` | Android lib | The DataStore snapshot, startup and fetch; the key registry by multibinding |
| `:remoteconfig:source-http` | Android lib | `remote-config/values.json`, read from GitHub raw over the app's OkHttp client |
| `:remoteconfig:bundle` | Android lib | No code: its dependency list decides which source ships |
| `:remoteconfig:debug` | Compose lib | Overrides on the device, in debug builds only |

Three rules would join the [dependency rules](../architecture.md#dependency-rules):

- **7.** Keys are declared only in `*:data`, inside an `internal object <Context>RemoteConfig`.
- **8.** Only a `source-*` module sees a transport or a vendor. Only the bundle depends on one, and `:app`
  depends on the bundle.
- **9.** `*:domain` and `*:ui` never depend on `:remoteconfig:*`.

## Each context owns its keys

![Ownership: each context declares its keys in an internal object of its data module and publishes them as domain decisions; the catalog screen reads the favorites context's IsFavoritesEnabled decision, never its key.](diagrams/remote-config-ownership.png)

| Key | Owner | Activation | Decision | Read by |
|---|---|---|---|---|
| `favorites_enabled` | `:favorites:data` | NextLaunch | `IsFavoritesEnabled` | `:catalog:ui`, which hides the heart and the favorites button |
| `catalog_pinned_categories` | `:catalog:data` | NextLaunch | `PinnedCategories` | `ObserveCategories`: pinned first, then alphabetical |
| `catalog_retry_on_reconnect` | `:catalog:data` | Immediate | `ObserveRetryOnReconnect` | `CatalogViewModel`, on reconnect |

The data module binds the decision the same way it binds its use cases today:

```kotlin
fun interface IsFavoritesEnabled {
    operator fun invoke(): Boolean
}

internal object FavoritesRemoteConfig {
    val Enabled = ConfigKey.bool("favorites_enabled", default = true)
    val all: Set<ConfigKey<*>> = setOf(Enabled)
}

@Module
@InstallIn(SingletonComponent::class)
internal object FavoritesRemoteConfigModule {

    @Provides
    fun provideIsFavoritesEnabled(remoteConfig: RemoteConfig): IsFavoritesEnabled =
        IsFavoritesEnabled { remoteConfig[FavoritesRemoteConfig.Enabled] }

    @Provides
    @ElementsIntoSet
    fun provideKeys(): Set<ConfigKey<*>> = FavoritesRemoteConfig.all
}
```

The catalog reading favorites' decision is the integration in presentation the contexts already use
([decision 3](../decisions.md#3-contexts-integrate-in-presentation)). A key's prefix matches its context, so a
guardrail test can check ownership.

## Tests without a mocking library

The test double is the production resolution over an in-memory map. It parses and falls back exactly as a
release build does:

```kotlin
class InMemoryRemoteConfig private constructor(
    private val values: MutableMap<String, String>,
    private val changes: MutableSharedFlow<Unit>,
) : RemoteConfig by LayeredRemoteConfig(
    overrides = RawValues.Empty,
    session = RawValues { values[it] },
    latest = RawValues { values[it] },
    activations = changes,
) {
    constructor() : this(mutableMapOf(), MutableSharedFlow(extraBufferCapacity = 1))

    operator fun <T : Any> set(key: ConfigKey<T>, value: T) {
        values[key.key] = key.type.encode(value)
        changes.tryEmit(Unit)
    }

    fun setRaw(key: ConfigKey<*>, raw: String) {
        values[key.key] = raw
        changes.tryEmit(Unit)
    }
}
```

- **ViewModels and use cases** take a decision lambda, as they already do
  ([test doubles](../testing.md#test-doubles)): `IsFavoritesEnabled { false }`.
- **Data modules** bind against `InMemoryRemoteConfig`, malformed values included.
- **The runtime** runs against a real DataStore on a temporary file, as `:core:settings` does.
- **The HTTP source** runs against MockWebServer with the real payload shape.

## Two sources, one line apart

- **Now: JSON over HTTP.** `remote-config/values.json` lives in this repository and is read from GitHub
  raw. A flag change is a reviewed pull request, and the file never holds a secret. It has no targeting and
  no percentage rollout, and raw content is cached for a few minutes.
- **Stage D: Firebase Remote Config.** It is a second `source-*` module that fetches, activates and returns
  only `VALUE_SOURCE_REMOTE` values as strings. Swapping is one line in `:remoteconfig:bundle`. The runtime
  keeps owning activation and defaults, so Firebase stays a transport.

## Trade-offs

- **Gained.**
  - A read cannot crash and cannot disagree between builds.
  - Defaults live next to their key, so none can be missing or misspelled.
  - Ownership is a visibility modifier, not a convention.
  - The vendor is a one-line choice.
  - The test double is the real rule.
- **Paid.**
  - Five new modules for three keys, seven with Firebase and the guardrails.
  - A domain `fun interface` for each decision.
  - The snapshot is stored twice when Firebase is the source, because Firebase keeps its own cache.
  - Values change at the next launch, not instantly.
- **Otherwise.**
  - With one context and a handful of flags, a single `Flags` object over Firebase is enough.
  - With experiments that need targeting from day one, Firebase or a dedicated service goes first, and the
    JSON source never exists.
