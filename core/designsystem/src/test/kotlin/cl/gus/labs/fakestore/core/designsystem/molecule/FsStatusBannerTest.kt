package cl.gus.labs.fakestore.core.designsystem.molecule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.registerRoborazziActivityToRobolectricIfNeeded
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
class FsStatusBannerTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `shows the uppercased message in a polite live region when visible`() {
        composeRule.setContent {
            FakeStoreTheme { FsStatusBanner(visible = true, message = "sin conexión") }
        }
        composeRule.onNodeWithText("SIN CONEXIÓN").assertExists()
        // liveRegion lives on the Row, not the Text node, since neither merges its descendants.
        composeRule
            .onNode(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
            .assertExists()
    }

    @Test
    fun `is absent when hidden`() {
        composeRule.setContent {
            FakeStoreTheme { FsStatusBanner(visible = false, message = "sin conexión") }
        }
        composeRule.onNodeWithText("SIN CONEXIÓN").assertDoesNotExist()
    }

    @Test
    fun `shows the banner after switching from hidden to visible`() {
        composeRule.setContent {
            FakeStoreTheme {
                var visible by remember { mutableStateOf(false) }
                Column {
                    FsStatusBanner(visible = visible, message = "sin conexión")
                    Text(
                        text = "toggle",
                        modifier = Modifier.clickable { visible = true },
                    )
                }
            }
        }
        composeRule.onNodeWithText("SIN CONEXIÓN").assertDoesNotExist()
        composeRule.onNodeWithText("toggle").performClick()
        composeRule.waitUntil { composeRule.onAllNodesWithText("SIN CONEXIÓN").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithText("SIN CONEXIÓN").assertExists()
    }
}
