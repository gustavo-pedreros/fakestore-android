package cl.gus.labs.fakestore.core.testing

import org.robolectric.annotation.Config
import org.robolectric.pluginapi.config.GlobalConfigProvider

private const val SDK_PROPERTY = "fakestore.robolectric.sdk"

/**
 * Robolectric's base config in every module: the SDK that `fakestore.android.robolectric` passes from the
 * version catalog. A module's `robolectric.properties` or a `@Config` still override it. Registered in
 * `META-INF/services`; it wins over Robolectric's own default, which has the lowest priority.
 */
class RobolectricDefaults : GlobalConfigProvider {
    override fun get(): Config {
        val sdk = checkNotNull(System.getProperty(SDK_PROPERTY)) {
            "$SDK_PROPERTY is not set: apply the fakestore.android.robolectric plugin"
        }
        return Config.Builder().setSdk(sdk.toInt()).build()
    }
}
