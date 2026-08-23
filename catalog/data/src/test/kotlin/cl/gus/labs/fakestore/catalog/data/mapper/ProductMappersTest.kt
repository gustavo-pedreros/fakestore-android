package cl.gus.labs.fakestore.catalog.data.mapper

import cl.gus.labs.fakestore.catalog.data.dto.ProductDto
import cl.gus.labs.fakestore.catalog.data.dto.RatingDto
import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.catalog.domain.model.Rating
import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import cl.gus.labs.fakestore.shared.kernel.ProductId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ProductMappers")
class ProductMappersTest {

    @Nested
    @DisplayName("ProductDto.toEntity")
    inner class ToEntity {

        @Test
        @DisplayName("flattens the nested rating and renames image to imageUrl")
        fun flattensRatingAndRenamesImage() {
            val dto = ProductDto(
                id = 1,
                title = "widget",
                price = 9.99,
                description = "a widget",
                category = "misc",
                imageUrl = "https://example.com/widget.png",
                rating = RatingDto(rate = 4.5, count = 10),
            )

            val entity = dto.toEntity()

            assertEquals(
                ProductEntity(
                    id = 1,
                    title = "widget",
                    price = 9.99,
                    description = "a widget",
                    category = "misc",
                    imageUrl = "https://example.com/widget.png",
                    ratingRate = 4.5,
                    ratingCount = 10,
                ),
                entity,
            )
        }
    }

    @Nested
    @DisplayName("ProductEntity.toDomain")
    inner class ToDomain {

        @Test
        @DisplayName("wraps id, category and the flattened rating in domain types")
        fun wrapsIdCategoryAndRating() {
            val entity = ProductEntity(
                id = 1,
                title = "widget",
                price = 9.99,
                description = "a widget",
                category = "misc",
                imageUrl = "https://example.com/widget.png",
                ratingRate = 4.5,
                ratingCount = 10,
            )

            val product = entity.toDomain()

            assertEquals(
                Product(
                    id = ProductId(1),
                    title = "widget",
                    price = 9.99,
                    description = "a widget",
                    category = Category("misc"),
                    imageUrl = "https://example.com/widget.png",
                    rating = Rating(rate = 4.5, count = 10),
                ),
                product,
            )
        }
    }
}
