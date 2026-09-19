package cl.gus.labs.fakestore.core.connectivity

import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowConnectivityManager
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ConnectivityNetworkMonitorTest {
    private lateinit var context: Context
    private lateinit var manager: ConnectivityManager
    private lateinit var shadowManager: ShadowConnectivityManager
    private lateinit var activeNetwork: Network

    private val wifi: Network = ShadowNetwork.newInstance(100)
    private val cellular: Network = ShadowNetwork.newInstance(200)

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        manager = context.getSystemService(ConnectivityManager::class.java)
        shadowManager = shadowOf(manager)
        shadowManager.setDefaultNetworkActive(true)
        activeNetwork = manager.activeNetwork!!
    }

    @Test
    fun `emits false when the active network is not validated`() = runTest {
        monitor().isOnline.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits true when the active network is already validated at collection time`() = runTest {
        shadowManager.setNetworkCapabilities(activeNetwork, usableCapabilities())

        monitor().isOnline.test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits false when there is no active network at all`() = runTest {
        shadowManager.clearAllNetworks()

        monitor().isOnline.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits false when the active network reports no capabilities`() = runTest {
        shadowManager.setNetworkCapabilities(activeNetwork, null)

        monitor().isOnline.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits true once a network gains validated internet`() = runTest {
        monitor().isOnline.test {
            assertFalse(awaitItem())

            notifyCapabilitiesChanged(wifi, usableCapabilities())

            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stays offline for a network with internet but no validation`() = runTest {
        monitor().isOnline.test {
            assertFalse(awaitItem())

            // A captive portal: the radio is up and routable, but nothing useful is reachable.
            notifyCapabilitiesChanged(wifi, capabilities(NetworkCapabilities.NET_CAPABILITY_INTERNET))

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits false when the only usable network loses its capabilities`() = runTest {
        monitor().isOnline.test {
            assertFalse(awaitItem())
            notifyCapabilitiesChanged(wifi, usableCapabilities())
            assertTrue(awaitItem())

            notifyCapabilitiesChanged(wifi, capabilities(NetworkCapabilities.NET_CAPABILITY_INTERNET))

            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stays online while a second usable network survives`() = runTest {
        monitor().isOnline.test {
            assertFalse(awaitItem())
            notifyCapabilitiesChanged(wifi, usableCapabilities())
            notifyCapabilitiesChanged(cellular, usableCapabilities())
            assertTrue(awaitItem())

            notifyLost(wifi)

            expectNoEvents()

            notifyLost(cellular)

            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ignores the loss of a network that was never usable`() = runTest {
        monitor().isOnline.test {
            assertFalse(awaitItem())
            notifyCapabilitiesChanged(wifi, usableCapabilities())
            assertTrue(awaitItem())

            notifyLost(cellular)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not re-emit while the online state is unchanged`() = runTest {
        monitor().isOnline.test {
            assertFalse(awaitItem())
            notifyCapabilitiesChanged(wifi, usableCapabilities())
            assertTrue(awaitItem())

            notifyCapabilitiesChanged(wifi, usableCapabilities())
            notifyCapabilitiesChanged(cellular, usableCapabilities())

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unregisters the network callback once collection stops`() = runTest {
        monitor().isOnline.test {
            assertFalse(awaitItem())

            assertEquals(1, shadowManager.networkCallbacks.size)

            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(shadowManager.networkCallbacks.isEmpty())
    }

    @Test
    fun `emits false and completes when the connectivity service is unavailable`() = runTest {
        val monitor = ConnectivityNetworkMonitor(
            context = contextWithoutConnectivityService(),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        monitor.isOnline.test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    private fun TestScope.monitor() =
        ConnectivityNetworkMonitor(context, UnconfinedTestDispatcher(testScheduler))

    private fun notifyCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
        shadowManager.networkCallbacks.forEach { it.onCapabilitiesChanged(network, capabilities) }
    }

    private fun notifyLost(network: Network) {
        shadowManager.networkCallbacks.forEach { it.onLost(network) }
    }

    private fun usableCapabilities(): NetworkCapabilities = capabilities(
        NetworkCapabilities.NET_CAPABILITY_INTERNET,
        NetworkCapabilities.NET_CAPABILITY_VALIDATED,
    )

    private fun capabilities(vararg granted: Int): NetworkCapabilities =
        ShadowNetworkCapabilities.newInstance().also { capabilities ->
            granted.forEach { shadowOf(capabilities).addCapability(it) }
        }

    private fun contextWithoutConnectivityService(): Context =
        object : ContextWrapper(RuntimeEnvironment.getApplication()) {
            override fun getSystemService(name: String): Any? =
                if (name == Context.CONNECTIVITY_SERVICE) null else super.getSystemService(name)
        }
}
