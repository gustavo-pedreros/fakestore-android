package cl.gus.labs.fakestore.core.network.config

import kotlinx.serialization.json.Json

internal val NetworkJson: Json = Json {
    ignoreUnknownKeys = true
}
