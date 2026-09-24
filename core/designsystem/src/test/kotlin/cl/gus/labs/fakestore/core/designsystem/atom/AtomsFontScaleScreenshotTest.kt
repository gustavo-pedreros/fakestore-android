package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.registerRoborazziActivityToRobolectricIfNeeded
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
class AtomsFontScaleScreenshotTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `atoms at font scale 2`() {
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(2f)) {
                FakeStoreTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(FakeStoreTheme.spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.md),
                        ) {
                            FsButton(text = "Reintentar", onClick = {})
                            FsChip(label = "Electronics", selected = true, onClick = {})
                            FsFavoriteButton(
                                checked = true,
                                onCheckedChange = {},
                                contentDescription = "Quitar de favoritos",
                            )
                            FsCategoryBadge(category = "women's clothing")
                            FsPriceText(amount = 1099.95)
                            FsRatingStars(rate = 4.8, count = 679)
                        }
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage(filePath = "atom/AtomsFontScale2.png")
    }
}
