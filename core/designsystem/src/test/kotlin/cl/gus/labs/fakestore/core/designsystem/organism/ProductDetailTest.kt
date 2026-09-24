package cl.gus.labs.fakestore.core.designsystem.organism

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cl.gus.labs.fakestore.core.designsystem.model.ProductDetailUiModel
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.registerRoborazziActivityToRobolectricIfNeeded
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
class ProductDetailTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    private val detail = ProductDetailUiModel(
        id = 1,
        title = "Mochila Foldsack No. 1",
        category = "men's clothing",
        description = "Tu mochila perfecta para el día a día y paseos por el bosque.",
        price = 109.95,
        rate = 3.9,
        ratingCount = 120,
        imageUrl = null,
    )

    @Test
    fun `describes the favorite toggle as add when not a favorite`() {
        composeRule.setContent {
            FakeStoreTheme {
                ProductDetailHeader(imageUrl = null, isFavorite = false, onFavoriteClick = {})
            }
        }
        composeRule.onNodeWithContentDescription("Añadir a favoritos").assertExists()
    }

    @Test
    fun `describes the favorite toggle as remove when a favorite`() {
        composeRule.setContent {
            FakeStoreTheme {
                ProductDetailHeader(imageUrl = null, isFavorite = true, onFavoriteClick = {})
            }
        }
        composeRule.onNodeWithContentDescription("Quitar de favoritos").assertExists()
    }

    @Test
    fun `toggling the favorite fires onFavoriteClick with the flipped value`() {
        var favorite: Boolean? = null
        composeRule.setContent {
            FakeStoreTheme {
                ProductDetailHeader(
                    imageUrl = null,
                    isFavorite = false,
                    onFavoriteClick = { favorite = it },
                )
            }
        }
        composeRule.onNodeWithContentDescription("Añadir a favoritos").performClick()
        assertEquals(true, favorite)
    }

    @Test
    fun `disables the favorite toggle while saving`() {
        composeRule.setContent {
            FakeStoreTheme {
                ProductDetailHeader(
                    imageUrl = null,
                    isFavorite = false,
                    onFavoriteClick = {},
                    savingFavorite = true,
                )
            }
        }
        composeRule.onNodeWithContentDescription("Añadir a favoritos").assertIsNotEnabled()
    }

    @Test
    fun `the body shows the title, uppercased category, price and description`() {
        composeRule.setContent {
            FakeStoreTheme { ProductDetailBody(product = detail) }
        }
        composeRule.onNodeWithText(detail.title).assertExists()
        composeRule.onNodeWithText("MEN'S CLOTHING").assertExists()
        composeRule.onNodeWithText("$109.95").assertExists()
        composeRule.onNodeWithText(detail.description).assertExists()
    }

    @Test
    fun `the body exposes the rating description`() {
        composeRule.setContent {
            FakeStoreTheme { ProductDetailBody(product = detail) }
        }
        composeRule.onNodeWithContentDescription("3.9 de 5 estrellas, 120 valoraciones").assertExists()
    }
}
