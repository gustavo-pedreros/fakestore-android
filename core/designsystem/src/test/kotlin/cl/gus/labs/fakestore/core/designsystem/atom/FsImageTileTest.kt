package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import cl.gus.labs.fakestore.core.designsystem.TestImages
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
class FsImageTileTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `a loaded image shows only the image, with its description`() {
        composeRule.setContent {
            FakeStoreTheme { FsImageTile(url = TestImages.Loaded, contentDescription = "Camiseta") }
        }
        composeRule.waitUntil { composeRule.onAllNodes(hasContentDescription("Camiseta")).fetchSemanticsNodes().isNotEmpty() }

        composeRule.onNodeWithTag(ImageTileImageTag).assertExists()
        composeRule.onAllNodes(hasTestTag(ImageTileMissingIconTag)).assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Camiseta").assertExists()
    }

    @Test
    fun `a broken image shows the missing-image icon, with the same description`() {
        composeRule.setContent {
            FakeStoreTheme { FsImageTile(url = TestImages.Broken, contentDescription = "Camiseta") }
        }
        composeRule.waitUntil { composeRule.onAllNodes(hasContentDescription("Camiseta")).fetchSemanticsNodes().isNotEmpty() }

        composeRule.onNodeWithTag(ImageTileMissingIconTag).assertExists()
        composeRule.onAllNodes(hasTestTag(ImageTileImageTag)).assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Camiseta").assertExists()
    }

    @Test
    fun `an empty url shows the missing-image icon, with the same description`() {
        composeRule.setContent {
            FakeStoreTheme { FsImageTile(url = null, contentDescription = "Camiseta") }
        }
        composeRule.waitUntil { composeRule.onAllNodes(hasContentDescription("Camiseta")).fetchSemanticsNodes().isNotEmpty() }

        composeRule.onNodeWithTag(ImageTileMissingIconTag).assertExists()
        composeRule.onAllNodes(hasTestTag(ImageTileImageTag)).assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Camiseta").assertExists()
    }

    @Test
    fun `a pending image shows neither the picture nor the missing-image icon`() {
        composeRule.setContent {
            FakeStoreTheme { FsImageTile(url = TestImages.Pending, contentDescription = "Camiseta") }
        }
        composeRule.onAllNodes(hasTestTag(ImageTileImageTag)).assertCountEquals(0)
        composeRule.onAllNodes(hasTestTag(ImageTileMissingIconTag)).assertCountEquals(0)
    }
}
