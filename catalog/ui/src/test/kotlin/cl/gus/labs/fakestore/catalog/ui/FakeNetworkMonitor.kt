package cl.gus.labs.fakestore.catalog.ui

import cl.gus.labs.fakestore.core.connectivity.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeNetworkMonitor(
    override val isOnline: MutableStateFlow<Boolean> = MutableStateFlow(true),
) : NetworkMonitor
