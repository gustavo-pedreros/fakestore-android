package cl.gus.labs.fakestore.core.designsystem.organism

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import cl.gus.labs.fakestore.core.designsystem.model.FsUiState
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
class FsStateHostTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    private fun setHost(state: FsUiState<String>) {
        composeRule.setContent {
            FakeStoreTheme {
                FsStateHost(
                    state = state,
                    loading = { Text("LOADING") },
                    empty = { Text("EMPTY") },
                    failure = { message -> Text("FAILURE: $message") },
                    content = { data -> Text("CONTENT: $data") },
                )
            }
        }
    }

    @Test
    fun `renders only the loading slot when loading`() {
        setHost(FsUiState.Loading)
        composeRule.onNodeWithText("LOADING").assertExists()
        composeRule.onNodeWithText("EMPTY").assertDoesNotExist()
        composeRule.onNodeWithText("FAILURE: Sin conexión").assertDoesNotExist()
        composeRule.onNodeWithText("CONTENT: catálogo").assertDoesNotExist()
    }

    @Test
    fun `renders only the empty slot when empty`() {
        setHost(FsUiState.Empty)
        composeRule.onNodeWithText("EMPTY").assertExists()
        composeRule.onNodeWithText("LOADING").assertDoesNotExist()
        composeRule.onNodeWithText("FAILURE: Sin conexión").assertDoesNotExist()
        composeRule.onNodeWithText("CONTENT: catálogo").assertDoesNotExist()
    }

    @Test
    fun `renders only the failure slot, and passes it the message`() {
        setHost(FsUiState.Failure("Sin conexión"))
        composeRule.onNodeWithText("FAILURE: Sin conexión").assertExists()
        composeRule.onNodeWithText("LOADING").assertDoesNotExist()
        composeRule.onNodeWithText("EMPTY").assertDoesNotExist()
        composeRule.onNodeWithText("CONTENT: catálogo").assertDoesNotExist()
    }

    @Test
    fun `renders only the content slot, and passes it the data`() {
        setHost(FsUiState.Content("catálogo"))
        composeRule.onNodeWithText("CONTENT: catálogo").assertExists()
        composeRule.onNodeWithText("LOADING").assertDoesNotExist()
        composeRule.onNodeWithText("EMPTY").assertDoesNotExist()
        composeRule.onNodeWithText("FAILURE: Sin conexión").assertDoesNotExist()
    }
}
