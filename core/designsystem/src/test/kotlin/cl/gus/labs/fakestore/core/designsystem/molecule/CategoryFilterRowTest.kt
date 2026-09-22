package cl.gus.labs.fakestore.core.designsystem.molecule

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.registerRoborazziActivityToRobolectricIfNeeded
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
class CategoryFilterRowTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    private val categories = listOf("jewelery", "electronics")

    @Test
    fun `selects Todos when nothing is selected`() {
        composeRule.setContent {
            FakeStoreTheme {
                CategoryFilterRow(categories = categories, selected = null, onSelect = {})
            }
        }
        composeRule.onNodeWithText("TODOS").assertIsSelected()
        composeRule.onNodeWithText("JEWELERY").assertIsNotSelected()
    }

    @Test
    fun `selects the matching category chip`() {
        composeRule.setContent {
            FakeStoreTheme {
                CategoryFilterRow(categories = categories, selected = "electronics", onSelect = {})
            }
        }
        composeRule.onNodeWithText("TODOS").assertIsNotSelected()
        composeRule.onNodeWithText("ELECTRONICS").assertIsSelected()
        composeRule.onNodeWithText("JEWELERY").assertIsNotSelected()
    }

    @Test
    fun `clicking Todos selects null`() {
        var selected: String? = "electronics"
        composeRule.setContent {
            FakeStoreTheme {
                CategoryFilterRow(categories = categories, selected = "electronics", onSelect = { selected = it })
            }
        }
        composeRule.onNodeWithText("TODOS").performClick()
        assertNull(selected)
    }

    @Test
    fun `clicking a category selects it`() {
        var selected: String? = null
        composeRule.setContent {
            FakeStoreTheme {
                CategoryFilterRow(categories = categories, selected = null, onSelect = { selected = it })
            }
        }
        composeRule.onNodeWithText("ELECTRONICS").performClick()
        assertEquals("electronics", selected)
    }
}
