package cl.gus.labs.fakestore.analytics.tool.logcat

/**
 * A thin seam over the Logcat SDK.
 *
 * Vendor SDKs are usually reached through a static singleton (`Logcat.getInstance()`), which
 * cannot be substituted in a unit test without static mocking. This interface exists purely so
 * [LogcatTracker] — where the mapping logic that is worth testing lives — can be tested with
 * a plain fake.
 *
 * Keep it dumb: no mapping, no filtering, one method per SDK call.
 */
interface LogcatClient {
    fun logEvent(name: String, properties: Map<String, String>)
    fun setUserId(userId: String)
    fun setUserProperty(key: String, value: String)
    fun reset()
}
