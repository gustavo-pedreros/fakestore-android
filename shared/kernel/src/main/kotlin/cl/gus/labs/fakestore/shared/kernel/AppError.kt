package cl.gus.labs.fakestore.shared.kernel

sealed interface AppError {
    data class Http(val httpStatus: Int, val rawBody: String?) : AppError

    data class Network(val message: String?) : AppError

    data object EmptyBody : AppError

    data class Unknown(val message: String?) : AppError
}