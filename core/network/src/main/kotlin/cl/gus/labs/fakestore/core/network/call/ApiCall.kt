package cl.gus.labs.fakestore.core.network.call

import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.core.network.error.toAppError
import cl.gus.labs.fakestore.shared.kernel.AppError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

suspend inline fun <T> executeCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    crossinline block: suspend () -> Response<T>,
): Either<AppError, T> = withContext(dispatcher) {
    try {
        val response = block()
        when {
            response.isSuccessful -> {
                val body = response.body()
                if (body != null) Either.Success(body) else Either.Error(AppError.EmptyBody)
            }
            else -> Either.Error(response.toAppError())
        }
    } catch (e: Exception) {
        Either.Error(e.toAppError())
    }
}