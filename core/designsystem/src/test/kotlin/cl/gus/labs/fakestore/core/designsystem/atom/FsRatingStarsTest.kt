package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class FsRatingStarsTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `exposes a single node with the singular description`() {
        composeRule.setContent {
            FakeStoreTheme { FsRatingStars(rate = 3.9, count = 1) }
        }
        composeRule.onNodeWithContentDescription("3.9 de 5 estrellas, 1 valoración").assertExists()
    }

    @Test
    fun `pluralizes the description for more than one rating`() {
        composeRule.setContent {
            FakeStoreTheme { FsRatingStars(rate = 4.8, count = 679) }
        }
        composeRule.onNodeWithContentDescription("4.8 de 5 estrellas, 679 valoraciones").assertExists()
    }

    @Test
    fun `has no separate text nodes`() {
        composeRule.setContent {
            FakeStoreTheme { FsRatingStars(rate = 3.9, count = 120) }
        }
        composeRule.onAllNodes(hasText("3.9")).assertCountEquals(0)
        composeRule.onAllNodes(hasText("(120)")).assertCountEquals(0)
    }
}