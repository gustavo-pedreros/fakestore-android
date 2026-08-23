package cl.gus.labs.fakestore.catalog.domain.usecase

import cl.gus.labs.fakestore.catalog.domain.CatalogRepository
import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.catalog.domain.model.Rating
import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.shared.kernel.AppError
import cl.gus.labs.fakestore.shared.kernel.ProductId
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ObserveCategories")
class ObserveCategoriesTest {

    @Nested
    @DisplayName("invoke")
    inner class Invoke {

        @Test
        @DisplayName("deduplicates repeated categories")
        fun deduplicatesRepeatedCategories() = runTest {
            val repository = FakeCatalogRepository(
                listOf(
                    product(id = 1, category = "electronics"),
                    product(id = 2, category = "electronics"),
                    product(id = 3, category = "jewelery"),
                ),
            )

            val result = ObserveCategories(repository)().first()

            assertEquals(listOf(Category("electronics"), Category("jewelery")), result)
        }

        @Test
        @DisplayName("sorts categories alphabetically")
        fun sortsCategoriesAlphabetically() = runTest {
            val repository = FakeCatalogRepository(
                listOf(
                    product(id = 1, category = "women's clothing"),
                    product(id = 2, category = "electronics"),
                    product(id = 3, category = "jewelery"),
                ),
            )

            val result = ObserveCategories(repository)().first()

            assertEquals(
                listOf(Category("electronics"), Category("jewelery"), Category("women's clothing")),
                result,
            )
        }

        @Test
        @DisplayName("returns an empty list for an empty catalog")
        fun returnsEmptyListForEmptyCatalog() = runTest {
            val repository = FakeCatalogRepository(emptyList())

            val result = ObserveCategories(repository)().first()

            assertEquals(emptyList<Category>(), result)
        }
    }
}

private fun product(id: Int, category: String) = Product(
    id = ProductId(id),
    title = "title-$id",
    price = 9.99,
    description = "description",
    category = Category(category),
    imageUrl = "https://example.com/$id.png",
    rating = Rating(rate = 4.0, count = 10),
)

private class FakeCatalogRepository(private val products: List<Product>) : CatalogRepository {
    override fun observeAll(category: Category?): Flow<List<Product>> = flowOf(products)
    override fun observeById(id: ProductId): Flow<Product?> = flowOf(null)
    override fun observeLastSyncedAt(): Flow<Instant?> = flowOf(null)
    override suspend fun refresh(): Either<AppError, Unit> = Either.Success(Unit)
}
