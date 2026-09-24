package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.registerRoborazziActivityToRobolectricIfNeeded
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
class FsButtonTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `uppercases its label`() {
        composeRule.setContent {
            FakeStoreTheme { FsButton(text = "reintentar", onClick = {}) }
        }
        composeRule.onNodeWithText("REINTENTAR").assertExists()
    }

    @Test
    fun `exposes the Button role`() {
        composeRule.setContent {
            FakeStoreTheme { FsButton(text = "Ver todo", onClick = {}) }
        }
        composeRule.onNodeWithText("VER TODO")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    @Test
    fun `fires onClick when enabled`() {
        var clicked = false
        composeRule.setContent {
            FakeStoreTheme { FsButton(text = "Ver", onClick = { clicked = true }) }
        }
        composeRule.onNodeWithText("VER").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `blocks the click when disabled`() {
        var clicked = false
        composeRule.setContent {
            FakeStoreTheme { FsButton(text = "Ver", onClick = { clicked = true }, enabled = false) }
        }
        composeRule.onNodeWithText("VER").assertIsNotEnabled()
        assertFalse(clicked)
    }

    @Test
    fun `blocks the click while loading`() {
        var clicked = false
        composeRule.setContent {
            FakeStoreTheme { FsButton(text = "Cargando", onClick = { clicked = true }, loading = true) }
        }
        composeRule.onNodeWithText("CARGANDO").assertIsNotEnabled()
        assertFalse(clicked)
    }

    @Test
    fun `shows an indeterminate progress bar while loading`() {
        composeRule.setContent {
            FakeStoreTheme { FsButton(text = "Cargando", onClick = {}, loading = true) }
        }
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun `shows no progress bar otherwise`() {
        composeRule.setContent {
            FakeStoreTheme { FsButton(text = "Ver", onClick = {}) }
        }
        composeRule.onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertCountEquals(0)
    }
}
