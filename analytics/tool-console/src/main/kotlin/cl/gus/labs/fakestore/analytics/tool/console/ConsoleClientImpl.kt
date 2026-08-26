package cl.gus.labs.fakestore.analytics.tool.console

import javax.inject.Inject
import javax.inject.Singleton

/** The only class that touches the output stream. Logic-free by design. */
@Singleton
class ConsoleClientImpl @Inject constructor() : ConsoleClient {
    override fun logEvent(name: String, properties: Map<String, String>) {
        println("[analytics] $name $properties")
    }
    override fun setUserId(userId: String) = println("[analytics] userId=$userId")
    override fun setUserProperty(key: String, value: String) = println("[analytics] $key=$value")
    override fun reset() = println("[analytics] reset")
}
