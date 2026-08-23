package cl.gus.labs.fakestore.catalog.domain.usecase

import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.shared.kernel.AppError

fun interface RefreshCatalog {
    suspend operator fun invoke(): Either<AppError, Unit>
}
