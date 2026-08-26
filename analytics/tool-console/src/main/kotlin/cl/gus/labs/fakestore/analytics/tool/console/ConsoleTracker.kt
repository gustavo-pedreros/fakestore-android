package cl.gus.labs.fakestore.analytics.tool.console

import cl.gus.labs.fakestore.analytics.AnalyticsEvent
import cl.gus.labs.fakestore.analytics.AnalyticsTracker
import cl.gus.labs.fakestore.analytics.AnalyticsUserSession
import cl.gus.labs.fakestore.analytics.Audience
import cl.gus.labs.fakestore.analytics.UserIdentity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maps analytics events onto the Console SDK.
 *
 * A tracker is a mapper and nothing else. It does not decide whether it should have received
 * an event — [audiences] declares what it serves and the dispatcher routes. Resist adding an
 * `if (event !is Something) return` here: that scatters routing across every vendor and makes
 * it testable only one vendor at a time.
 */
@Singleton
class ConsoleTracker @Inject constructor(
    private val client: ConsoleClient,
) : AnalyticsTracker, AnalyticsUserSession {

    /** Narrow this to the audiences Console is actually paid to receive. */
    override val audiences: Set<Audience> = setOf(Audience.Product, Audience.Marketing)

    override fun track(event: AnalyticsEvent) {
        client.logEvent(
            name = event.eventName.toVendorName(),
            properties = event.properties.mapValues { (_, value) -> value.toString() },
        )
    }

    override fun identify(user: UserIdentity) {
        client.setUserId(user.userId)
        user.traits.forEach { (key, value) -> client.setUserProperty(key, value.toString()) }
    }

    override fun clear() {
        client.reset()
    }

    /**
     * Most SDKs reject spaces or capitals in event names. Normalise here, in the vendor
     * module — never upstream in the event, whose name is the one humans and analysts read.
     */
    private fun String.toVendorName(): String = replace(" ", "_").lowercase()
}
