package cl.gus.labs.fakestore.favorites.domain.usecase

import cl.gus.labs.fakestore.shared.kernel.ProductId

fun interface ToggleFavorite {
    suspend operator fun invoke(id: ProductId)
}
