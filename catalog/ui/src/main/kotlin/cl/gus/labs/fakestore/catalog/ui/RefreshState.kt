package cl.gus.labs.fakestore.catalog.ui

import cl.gus.labs.fakestore.shared.kernel.AppError

internal sealed interface RefreshState {

    data object Idle : RefreshState

    data object InFlight : RefreshState

    data class Failed(val error: AppError) : RefreshState
}
