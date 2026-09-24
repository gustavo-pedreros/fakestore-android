package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
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

    @Test
    fun `drops the count when the row has no room for it`() {
        val offered = 140.dp
        composeRule.setContent {
            FakeStoreTheme {
                Column {
                    Box { FsRatingStars(rate = 3.9, count = 120) }
                    Box(Modifier.width(offered)) { FsRatingStars(rate = 3.9, count = 120) }
                }
            }
        }
        val nodes = composeRule.onAllNodesWithContentDescription("3.9 de 5 estrellas, 120 valoraciones")
        val roomy = nodes[0].fetchSemanticsNode().size.width
        val cramped = nodes[1].fetchSemanticsNode().size.width
        val offeredPx = with(composeRule.density) { offered.roundToPx() }

        assertTrue(cramped < roomy)
        // Dropping the count makes the row report back narrower than the space it was given;
        // clipping or wrapping it would instead fill that space.
        assertTrue(cramped < offeredPx)
    }
}