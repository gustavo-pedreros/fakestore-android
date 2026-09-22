package cl.gus.labs.fakestore.core.designsystem.organism

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
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
class ProductGridTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    private fun product(id: Int, title: String) = ProductCardUiModel(
        id = id,
        title = title,
        category = "men's clothing",
        price = 109.95,
        rate = 3.9,
        ratingCount = 120,
        imageUrl = null,
    )

    private val pair = listOf(product(1, "Mochila Foldsack"), product(2, "Camiseta Slim Fit"))

    @Test
    fun `renders one card per product`() {
        val products = pair + listOf(product(3, "Pulsera Naga"), product(4, "Disco WD 2TB"))
        composeRule.setContent {
            FakeStoreTheme {
                ProductGrid(
                    products = products,
                    favoriteIds = emptySet(),
                    onProductClick = {},
                    onFavoriteClick = { _, _ -> },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        products.forEach { product ->
            composeRule.onNode(hasScrollAction()).performScrollToNode(hasText(product.title))
            composeRule.onNodeWithText(product.title).assertExists()
        }
    }

    @Test
    fun `marks only the products in favoriteIds as a favorite`() {
        composeRule.setContent {
            FakeStoreTheme {
                ProductGrid(
                    products = pair,
                    favoriteIds = setOf(2),
                    onProductClick = {},
                    onFavoriteClick = { _, _ -> },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        composeRule.onAllNodesWithContentDescription("Quitar de favoritos").assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription("Añadir a favoritos").assertCountEquals(1)
    }

    @Test
    fun `disables the favorite toggle only for the products being saved`() {
        composeRule.setContent {
            FakeStoreTheme {
                ProductGrid(
                    products = pair,
                    favoriteIds = setOf(2),
                    onProductClick = {},
                    onFavoriteClick = { _, _ -> },
                    modifier = Modifier.fillMaxSize(),
                    savingFavoriteIds = setOf(1),
                )
            }
        }
        // Product 1 is saving, product 2 is an untouched favorite, so their descriptions differ.
        composeRule.onNodeWithContentDescription("Añadir a favoritos").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Quitar de favoritos").assertIsEnabled()
    }

    @Test
    fun `passes the product id to onProductClick`() {
        var clickedId: Int? = null
        composeRule.setContent {
            FakeStoreTheme {
                ProductGrid(
                    products = pair,
                    favoriteIds = emptySet(),
                    onProductClick = { clickedId = it },
                    onFavoriteClick = { _, _ -> },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        composeRule.onNodeWithText("Camiseta Slim Fit").performClick()
        assertEquals(2, clickedId)
    }

    @Test
    fun `passes the product id and the new value to onFavoriteClick`() {
        var received: Pair<Int, Boolean>? = null
        composeRule.setContent {
            FakeStoreTheme {
                ProductGrid(
                    products = pair,
                    favoriteIds = setOf(2),
                    onProductClick = {},
                    onFavoriteClick = { id, checked -> received = id to checked },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        // Product 1 is not a favorite, so toggling it asks to add it.
        composeRule.onNodeWithContentDescription("Añadir a favoritos").performClick()
        assertEquals(1 to true, received)

        composeRule.onNodeWithContentDescription("Quitar de favoritos").performClick()
        assertEquals(2 to false, received)
    }

    @Test
    fun `the skeleton cannot be scrolled`() {
        composeRule.setContent {
            FakeStoreTheme { ProductGridSkeleton(modifier = Modifier.fillMaxSize()) }
        }
        composeRule.onAllNodes(hasScrollAction()).assertCountEquals(0)
    }
}
