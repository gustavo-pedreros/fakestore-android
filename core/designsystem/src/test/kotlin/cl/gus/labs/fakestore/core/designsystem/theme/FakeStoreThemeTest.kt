package cl.gus.labs.fakestore.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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
class FakeStoreThemeTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `darkTheme true provides the dark color scheme and FakeStoreColors`() {
        var background = Color.Unspecified
        var colors: FakeStoreColors? = null
        composeRule.setContent {
            FakeStoreTheme(darkTheme = true) {
                background = MaterialTheme.colorScheme.background
                colors = FakeStoreTheme.colors
            }
        }
        assertEquals(Ink, background)
        assertEquals(FakeStoreColorsDark, colors)
    }

    @Test
    fun `darkTheme false provides the light color scheme and FakeStoreColors`() {
        var background = Color.Unspecified
        var colors: FakeStoreColors? = null
        composeRule.setContent {
            FakeStoreTheme(darkTheme = false) {
                background = MaterialTheme.colorScheme.background
                colors = FakeStoreTheme.colors
            }
        }
        assertEquals(Paper, background)
        assertEquals(FakeStoreColorsLight, colors)
    }

    @Test
    fun `switching darkTheme swaps the colors live`() {
        var dark by mutableStateOf(false)
        var background = Color.Unspecified
        composeRule.setContent {
            FakeStoreTheme(darkTheme = dark) {
                background = MaterialTheme.colorScheme.background
            }
        }
        assertEquals(Paper, background)

        dark = true
        composeRule.waitForIdle()
        assertEquals(Ink, background)
    }
}
