package cl.gus.labs.fakestore.analytics.tool.logcat

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/** The only class that touches the logging API. Logic-free by design. */
@Singleton
class LogcatClientImpl @Inject constructor() : LogcatClient {
    private companion object { const val TAG = "Analytics" }

    override fun logEvent(name: String, properties: Map<String, String>) {
        Log.d(TAG, "$name $properties")
    }
    override fun setUserId(userId: String) = Unit.also { Log.d(TAG, "userId=$userId") }
    override fun setUserProperty(key: String, value: String) = Unit.also { Log.d(TAG, "$key=$value") }
    override fun reset() = Unit.also { Log.d(TAG, "reset") }
}
