package cl.gus.labs.fakestore.core.designsystem.molecule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.TestImages
import cl.gus.labs.fakestore.core.designsystem.atom.ImageTileImageTag
import cl.gus.labs.fakestore.core.designsystem.atom.ImageTileMissingIconTag
import cl.gus.labs.fakestore.core.designsystem.icon.FsIcons
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
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

private val CardWidth = 173.dp

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
class MoleculesScreenshotTest {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    private val product = ProductCardUiModel(
        id = 1,
        title = "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
        category = "men's clothing",
        price = 109.95,
        rate = 3.9,
        ratingCount = 120,
        imageUrl = TestImages.Loaded,
    )

    @Test
    fun `product card with a loaded image, light`() {
        composeRule.setContent {
            FakeStoreTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ProductCard(
                        product = product,
                        isFavorite = false,
                        onClick = {},
                        onFavoriteClick = {},
                        modifier = Modifier
                            .padding(FakeStoreTheme.spacing.lg)
                            .width(CardWidth),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodes(hasTestTag(ImageTileImageTag), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onRoot().captureRoboImage(filePath = "molecule/MoleculesLoadedImage_Light.png")
    }

    @Test
    fun `product card with a loaded image, dark`() {
        composeRule.setContent {
            FakeStoreTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ProductCard(
                        product = product,
                        isFavorite = true,
                        onClick = {},
                        onFavoriteClick = {},
                        modifier = Modifier
                            .padding(FakeStoreTheme.spacing.lg)
                            .width(CardWidth),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodes(hasTestTag(ImageTileImageTag), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onRoot().captureRoboImage(filePath = "molecule/MoleculesLoadedImage_Dark.png")
    }

    @Test
    fun `molecules with long text`() {
        composeRule.setContent {
            FakeStoreTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(FakeStoreTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.lg),
                    ) {
                        FsDetailTopBar(
                            title = "Samsung 49-Inch CHG90 144Hz Curved Gaming Monitor (LC49HG90DMNXZA)",
                            titleVisible = true,
                            onBackClick = {},
                            onShareClick = {},
                        )
                        FsStatusBanner(
                            visible = true,
                            message = "Sin conexión: mostrando el catálogo guardado el 22 de agosto a las 14:30",
                        )
                        ProductCard(
                            product = product.copy(
                                title = "Samsung 49-Inch CHG90 144Hz Curved Gaming Monitor, Super Ultrawide",
                                imageUrl = null,
                            ),
                            isFavorite = false,
                            onClick = {},
                            onFavoriteClick = {},
                            modifier = Modifier.width(CardWidth),
                        )
                    }
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodes(hasTestTag(ImageTileMissingIconTag), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onRoot().captureRoboImage(filePath = "molecule/MoleculesLongText.png")
    }

    // Taller than the pinned viewport so the whole sheet is captured: at font scale 2 the stack
    // overflows 915dp and the card would be cropped before its rating row and price.
    @Config(qualifiers = "+h1600dp")
    @Test
    fun `molecules at font scale 2`() {
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(2f)) {
                FakeStoreTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(FakeStoreTheme.spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.lg),
                        ) {
                            CategoryFilterRow(
                                categories = listOf("jewelery", "electronics"),
                                selected = "electronics",
                                onSelect = {},
                            )
                            FsListTopBar(onFavoritesClick = {}, onThemeToggleClick = {})
                            FsDetailTopBar(
                                title = "Fjallraven Foldsack No. 1",
                                titleVisible = true,
                                onBackClick = {},
                                onShareClick = {},
                            )
                            FsStatusBanner(visible = true, message = "Sin conexión")
                            FsStateBlock(
                                icon = FsIcons.Alert,
                                title = "No pudimos cargar el catálogo",
                                body = "Revisa tu conexión y vuelve a intentarlo.",
                            )
                            ProductCard(
                                product = product.copy(imageUrl = null),
                                isFavorite = false,
                                onClick = {},
                                onFavoriteClick = {},
                                modifier = Modifier.width(CardWidth),
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodes(hasTestTag(ImageTileMissingIconTag), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onRoot().captureRoboImage(filePath = "molecule/MoleculesFontScale2.png")
    }
}
