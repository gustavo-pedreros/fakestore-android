package cl.gus.labs.fakestore.core.common.result

sealed interface Either<out L, out R> {

    data class Error<out L>(val error: L) : Either<L, Nothing>

    data class Success<out R>(val value: R) : Either<Nothing, R>

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}

inline fun <L, R, T> Either<L, R>.map(transform: (R) -> T): Either<L, T> = when (this) {
    is Either.Success -> Either.Success(transform(value))
    is Either.Error -> this
}

inline fun <L, R, T> Either<L, R>.flatMap(transform: (R) -> Either<L, T>): Either<L, T> = when (this) {
    is Either.Success -> transform(value)
    is Either.Error -> this
}

inline fun <L, R, T> Either<L, R>.mapError(transform: (L) -> T): Either<T, R> = when (this) {
    is Either.Success -> this
    is Either.Error -> Either.Error(transform(error))
}

inline fun <L, R, T> Either<L, R>.fold(onError: (L) -> T, onSuccess: (R) -> T): T = when (this) {
    is Either.Success -> onSuccess(value)
    is Either.Error -> onError(error)
}

fun <L, R> Either<L, R>.getOrNull(): R? = (this as? Either.Success)?.value

fun <L, R> Either<L, R>.errorOrNull(): L? = (this as? Either.Error)?.error
