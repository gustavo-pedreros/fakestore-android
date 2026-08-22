package cl.gus.labs.fakestore.core.network.error

import cl.gus.labs.fakestore.shared.kernel.AppError
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

fun Response<*>.toAppError(): AppError {
    val status = code()
    val raw = runCatching { errorBody()?.string() }.getOrNull()
    return AppError.Http(status, raw)
}

fun Throwable.toAppError(): AppError = when (this) {
    is HttpException -> response()?.toAppError() ?: AppError.Http(code(), message)
    is IOException -> AppError.Network(message)
    else -> AppError.Unknown(message)
}