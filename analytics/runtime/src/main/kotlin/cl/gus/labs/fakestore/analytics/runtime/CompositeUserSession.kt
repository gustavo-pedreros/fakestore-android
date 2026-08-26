package cl.gus.labs.fakestore.analytics.runtime

import cl.gus.labs.fakestore.analytics.AnalyticsUserSession
import cl.gus.labs.fakestore.analytics.UserIdentity

/**
 * Fans identity calls out to every vendor that registered one.
 *
 * Because [AnalyticsUserSession] defaults every method to a no-op, a vendor that does not
 * support identity simply does not override anything, and this class needs no knowledge of
 * which vendor supports what.
 */
class CompositeUserSession(
    private val sessions: Set<AnalyticsUserSession>,
) : AnalyticsUserSession {

    override fun identify(user: UserIdentity) {
        sessions.forEach { it.identify(user) }
    }

    override fun clear() {
        sessions.forEach { it.clear() }
    }
}
