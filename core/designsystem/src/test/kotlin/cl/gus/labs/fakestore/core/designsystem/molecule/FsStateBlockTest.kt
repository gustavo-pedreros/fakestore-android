package cl.gus.labs.fakestore.core.designsystem.molecule

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import cl.gus.labs.fakestore.core.designsystem.icon.FsIcons
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
class FsStateBlockTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `shows the title`() {
        composeRule.setContent {
            FakeStoreTheme { FsStateBlock(icon = FsIcons.Alert, title = "Sin conexión") }
        }
        composeRule.onNodeWithText("Sin conexión").assertExists()
    }

    @Test
    fun `shows the body only when given`() {
        composeRule.setContent {
            FakeStoreTheme {
                FsStateBlock(icon = FsIcons.Alert, title = "Sin conexión", body = "Revisa tu conexión")
            }
        }
        composeRule.onNodeWithText("Revisa tu conexión").assertExists()
    }

    @Test
    fun `omits the body when null`() {
        composeRule.setContent {
            FakeStoreTheme { FsStateBlock(icon = FsIcons.Alert, title = "Sin conexión", body = null) }
        }
        composeRule.onNodeWithText("Revisa tu conexión").assertDoesNotExist()
    }

    @Test
    fun `shows the action only when given`() {
        composeRule.setContent {
            FakeStoreTheme {
                FsStateBlock(
                    icon = FsIcons.Alert,
                    title = "Sin conexión",
                    action = { Text("Reintentar") },
                )
            }
        }
        composeRule.onNodeWithText("Reintentar").assertExists()
    }

    @Test
    fun `omits the action when null`() {
        composeRule.setContent {
            FakeStoreTheme { FsStateBlock(icon = FsIcons.Alert, title = "Sin conexión") }
        }
        composeRule.onNodeWithText("Reintentar").assertDoesNotExist()
    }
}
