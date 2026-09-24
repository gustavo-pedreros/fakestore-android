package cl.gus.labs.fakestore.core.designsystem.molecule

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.registerRoborazziActivityToRobolectricIfNeeded
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
class ProductCardTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    private val product = ProductCardUiModel(
        id = 1,
        title = "Fjallraven - Foldsack No. 1 Backpack",
        category = "men's clothing",
        price = 109.95,
        rate = 3.9,
        ratingCount = 120,
        imageUrl = null,
    )

    @Test
    fun `fires onClick`() {
        var clicked = false
        composeRule.setContent {
            FakeStoreTheme {
                ProductCard(product = product, isFavorite = false, onClick = { clicked = true }, onFavoriteClick = {})
            }
        }
        composeRule.onNodeWithText(product.title).performClick()
        assertTrue(clicked)
    }

    @Test
    fun `describes the favorite toggle as add when not a favorite`() {
        composeRule.setContent {
            FakeStoreTheme {
                ProductCard(product = product, isFavorite = false, onClick = {}, onFavoriteClick = {})
            }
        }
        composeRule.onNodeWithContentDescription("Añadir a favoritos").assertExists()
    }

    @Test
    fun `describes the favorite toggle as remove when a favorite`() {
        composeRule.setContent {
            FakeStoreTheme {
                ProductCard(product = product, isFavorite = true, onClick = {}, onFavoriteClick = {})
            }
        }
        composeRule.onNodeWithContentDescription("Quitar de favoritos").assertExists()
    }

    @Test
    fun `toggling the favorite fires onFavoriteClick with the flipped value`() {
        var favorite: Boolean? = null
        composeRule.setContent {
            FakeStoreTheme {
                ProductCard(
                    product = product,
                    isFavorite = false,
                    onClick = {},
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
                ProductCard(
                    product = product,
                    isFavorite = false,
                    onClick = {},
                    onFavoriteClick = {},
                    savingFavorite = true,
                )
            }
        }
        composeRule.onNodeWithContentDescription("Añadir a favoritos").assertIsNotEnabled()
    }

    @Test
    fun `shows the title, uppercased category, price and rating description`() {
        composeRule.setContent {
            FakeStoreTheme {
                ProductCard(product = product, isFavorite = false, onClick = {}, onFavoriteClick = {})
            }
        }
        composeRule.onNodeWithText(product.title).assertExists()
        composeRule.onNodeWithText("MEN'S CLOTHING").assertExists()
        composeRule.onNodeWithText("$109.95").assertExists()
        composeRule.onNodeWithContentDescription("3.9 de 5 estrellas, 120 valoraciones").assertExists()
    }

    @Test
    fun `the skeleton has no clickable node`() {
        composeRule.setContent {
            FakeStoreTheme { ProductCardSkeleton() }
        }
        composeRule.onAllNodes(hasClickAction()).assertCountEquals(0)
    }
}
