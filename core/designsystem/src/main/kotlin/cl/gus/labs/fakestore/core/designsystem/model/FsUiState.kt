package cl.gus.labs.fakestore.core.designsystem.model

sealed interface FsUiState<out T> {

    data object Loading : FsUiState<Nothing>

    data object Empty : FsUiState<Nothing>

    data class Failure(val message: String) : FsUiState<Nothing>

    data class Content<out T>(
        val data: T,
        val fromCache: Boolean = false,
    ) : FsUiState<T>
}
