package cl.gus.labs.fakestore.core.designsystem.molecule

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.registerRoborazziActivityToRobolectricIfNeeded
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
class FsTopBarTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `list bar reads FakeStore as its wordmark`() {
        composeRule.setContent {
            FakeStoreTheme { FsListTopBar(onFavoritesClick = {}) }
        }
        composeRule.onNodeWithContentDescription("FakeStore").assertExists()
    }

    @Test
    fun `list bar's favorites action fires its click`() {
        var clicked = false
        composeRule.setContent {
            FakeStoreTheme { FsListTopBar(onFavoritesClick = { clicked = true }) }
        }
        composeRule.onNodeWithContentDescription("Favoritos").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `list bar shows the theme toggle only with a callback`() {
        composeRule.setContent {
            FakeStoreTheme { FsListTopBar(onFavoritesClick = {}) }
        }
        composeRule.onNodeWithContentDescription("Cambiar tema").assertDoesNotExist()
    }

    @Test
    fun `list bar's theme toggle fires its click when given`() {
        var clicked = false
        composeRule.setContent {
            FakeStoreTheme {
                FsListTopBar(onFavoritesClick = {}, onThemeToggleClick = { clicked = true })
            }
        }
        composeRule.onNodeWithContentDescription("Cambiar tema").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `detail bar's back action fires its click`() {
        var clicked = false
        composeRule.setContent {
            FakeStoreTheme {
                FsDetailTopBar(title = "Producto", titleVisible = true, onBackClick = { clicked = true })
            }
        }
        composeRule.onNodeWithContentDescription("Volver").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `detail bar shows the share action only with a callback`() {
        composeRule.setContent {
            FakeStoreTheme {
                FsDetailTopBar(title = "Producto", titleVisible = true, onBackClick = {})
            }
        }
        composeRule.onNodeWithContentDescription("Compartir").assertDoesNotExist()
    }

    @Test
    fun `detail bar's share action fires its click when given`() {
        var clicked = false
        composeRule.setContent {
            FakeStoreTheme {
                FsDetailTopBar(
                    title = "Producto",
                    titleVisible = true,
                    onBackClick = {},
                    onShareClick = { clicked = true },
                )
            }
        }
        composeRule.onNodeWithContentDescription("Compartir").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `detail bar shows the title when visible`() {
        composeRule.setContent {
            FakeStoreTheme {
                FsDetailTopBar(title = "Producto", titleVisible = true, onBackClick = {})
            }
        }
        composeRule.onNodeWithText("Producto").assertExists()
    }

    @Test
    fun `detail bar hides the title when not visible`() {
        composeRule.setContent {
            FakeStoreTheme {
                FsDetailTopBar(title = "Producto", titleVisible = false, onBackClick = {})
            }
        }
        composeRule.onNodeWithText("Producto").assertDoesNotExist()
    }
}
