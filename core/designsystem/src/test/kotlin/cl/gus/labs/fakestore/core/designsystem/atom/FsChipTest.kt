package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.registerRoborazziActivityToRobolectricIfNeeded
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
class FsChipTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `uppercases its label`() {
        composeRule.setContent {
            FakeStoreTheme { FsChip(label = "jewelery", selected = false, onClick = {}) }
        }
        composeRule.onNodeWithText("JEWELERY").assertExists()
    }

    @Test
    fun `exposes the Tab role`() {
        composeRule.setContent {
            FakeStoreTheme { FsChip(label = "Todos", selected = true, onClick = {}) }
        }
        composeRule.onNodeWithText("TODOS")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
    }

    @Test
    fun `exposes the selected state`() {
        composeRule.setContent {
            FakeStoreTheme { FsChip(label = "Todos", selected = true, onClick = {}) }
        }
        composeRule.onNodeWithText("TODOS").assertIsSelected()
    }

    @Test
    fun `exposes the unselected state`() {
        composeRule.setContent {
            FakeStoreTheme { FsChip(label = "Jewelery", selected = false, onClick = {}) }
        }
        composeRule.onNodeWithText("JEWELERY").assertIsNotSelected()
    }

    @Test
    fun `fires onClick`() {
        var clicked = false
        composeRule.setContent {
            FakeStoreTheme { FsChip(label = "Jewelery", selected = false, onClick = { clicked = true }) }
        }
        composeRule.onNodeWithText("JEWELERY").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `blocks the click when disabled`() {
        var clicked = false
        composeRule.setContent {
            FakeStoreTheme {
                FsChip(label = "Electronics", selected = false, onClick = { clicked = true }, enabled = false)
            }
        }
        composeRule.onNodeWithText("ELECTRONICS").assertIsNotEnabled()
        assertFalse(clicked)
    }

    @Test
    fun `keeps a touch target of at least 48dp around the 44dp chip`() {
        composeRule.setContent {
            FakeStoreTheme { FsChip(label = "Todos", selected = false, onClick = {}) }
        }
        val node = composeRule.onNodeWithText("TODOS").fetchSemanticsNode()
        val minPx = with(composeRule.density) { 48.dp.toPx() } - 0.5f

        assertTrue(node.touchBoundsInRoot.width >= minPx)
        assertTrue(node.touchBoundsInRoot.height >= minPx)
        // The visual chip itself stays 44dp: minimumInteractiveComponentSize only pads the touch area.
        assertEquals(44.dp, with(composeRule.density) { node.boundsInRoot.height.toDp() })
    }
}
