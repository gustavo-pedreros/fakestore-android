package cl.gus.labs.fakestore.core.designsystem.organism

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import cl.gus.labs.fakestore.core.designsystem.TestImages
import cl.gus.labs.fakestore.core.designsystem.atom.ImageTileImageTag
import cl.gus.labs.fakestore.core.designsystem.atom.ImageTileMissingIconTag
import cl.gus.labs.fakestore.core.designsystem.model.ProductDetailUiModel
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
import org.robolectric.annotation.Config

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
class OrganismsScreenshotTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    private val detail = ProductDetailUiModel(
        id = 1,
        title = "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
        category = "men's clothing",
        description = "Your perfect pack for everyday use and walks in the forest. " +
            "Stash your laptop (up to 15 inches) in the padded sleeve, your everyday.",
        price = 109.95,
        rate = 3.9,
        ratingCount = 120,
        imageUrl = TestImages.Loaded,
    )

    @Test
    fun `product detail with a loaded image, light`() {
        setDetail(darkTheme = false, imageUrl = detail.imageUrl)
        awaitTile(ImageTileImageTag)
        composeRule.onRoot().captureRoboImage(filePath = "organism/ProductDetailLoadedImage_Light.png")
    }

    @Test
    fun `product detail with a loaded image, dark`() {
        setDetail(darkTheme = true, imageUrl = detail.imageUrl)
        awaitTile(ImageTileImageTag)
        composeRule.onRoot().captureRoboImage(filePath = "organism/ProductDetailLoadedImage_Dark.png")
    }

    // Taller than the pinned viewport so the whole detail is captured rather than cropped at 915dp.
    @Config(qualifiers = "+h1600dp")
    @Test
    fun `product detail at font scale 2`() {
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(2f)) {
                FakeStoreTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Column {
                            ProductDetailHeader(
                                imageUrl = null,
                                isFavorite = true,
                                onFavoriteClick = {},
                            )
                            ProductDetailBody(product = detail.copy(imageUrl = null))
                        }
                    }
                }
            }
        }
        awaitTile(ImageTileMissingIconTag)
        composeRule.onRoot().captureRoboImage(filePath = "organism/OrganismsFontScale2.png")
    }

    private fun setDetail(darkTheme: Boolean, imageUrl: String?) {
        composeRule.setContent {
            FakeStoreTheme(darkTheme = darkTheme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        ProductDetailHeader(
                            imageUrl = imageUrl,
                            isFavorite = true,
                            onFavoriteClick = {},
                        )
                        ProductDetailBody(product = detail.copy(imageUrl = imageUrl))
                    }
                }
            }
        }
    }

    // The tag sits under ProductDetailHeader's merged semantics, so the query has to skip merging.
    private fun awaitTile(tag: String) {
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodes(hasTestTag(tag), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
