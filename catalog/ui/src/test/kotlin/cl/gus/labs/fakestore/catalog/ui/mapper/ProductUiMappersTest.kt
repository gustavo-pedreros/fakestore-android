package cl.gus.labs.fakestore.catalog.ui.mapper

import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.catalog.domain.model.Rating
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
import cl.gus.labs.fakestore.core.designsystem.model.ProductDetailUiModel
import cl.gus.labs.fakestore.shared.kernel.ProductId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ProductUiMappers")
class ProductUiMappersTest {

    private val product = Product(
        id = ProductId(1),
        title = "widget",
        price = 9.99,
        description = "a widget",
        category = Category("misc"),
        imageUrl = "https://example.com/widget.png",
        rating = Rating(rate = 4.5, count = 10),
    )

    @Nested
    @DisplayName("Product.toCard")
    inner class ToCard {

        @Test
        @DisplayName("unwraps id, category and rating, and drops the description")
        fun unwrapsValueTypes() {
            val card = product.toCard()

            assertEquals(
                ProductCardUiModel(
                    id = 1,
                    title = "widget",
                    category = "misc",
                    price = 9.99,
                    rate = 4.5,
                    ratingCount = 10,
                    imageUrl = "https://example.com/widget.png",
                ),
                card,
            )
        }
    }

    @Nested
    @DisplayName("Product.toDetail")
    inner class ToDetail {

        @Test
        @DisplayName("unwraps id, category and rating, and keeps the description")
        fun unwrapsValueTypesAndKeepsDescription() {
            val detail = product.toDetail()

            assertEquals(
                ProductDetailUiModel(
                    id = 1,
                    title = "widget",
                    category = "misc",
                    description = "a widget",
                    price = 9.99,
                    rate = 4.5,
                    ratingCount = 10,
                    imageUrl = "https://example.com/widget.png",
                ),
                detail,
            )
        }
    }
}
