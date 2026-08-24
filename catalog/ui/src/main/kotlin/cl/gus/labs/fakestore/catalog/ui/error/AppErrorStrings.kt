package cl.gus.labs.fakestore.catalog.ui.error

import androidx.annotation.StringRes
import cl.gus.labs.fakestore.catalog.ui.R
import cl.gus.labs.fakestore.shared.kernel.AppError

internal data class AppErrorStrings(
    @param:StringRes val title: Int,
    @param:StringRes val body: Int,
)

internal fun appErrorStrings(error: AppError, offline: Boolean): AppErrorStrings =
    if (offline) {
        AppErrorStrings(
            title = R.string.catalog_error_offline_title,
            body = R.string.catalog_error_offline_body,
        )
    } else {
        when (error) {
            is AppError.Network -> AppErrorStrings(
                title = R.string.catalog_error_network_title,
                body = R.string.catalog_error_network_body,
            )

            is AppError.Http, AppError.EmptyBody -> AppErrorStrings(
                title = R.string.catalog_error_server_title,
                body = R.string.catalog_error_server_body,
            )

            is AppError.Unknown -> AppErrorStrings(
                title = R.string.catalog_error_unknown_title,
                body = R.string.catalog_error_unknown_body,
            )
        }
    }
