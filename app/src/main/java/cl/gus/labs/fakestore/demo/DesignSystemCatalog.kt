package cl.gus.labs.fakestore.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.atom.FsButton
import cl.gus.labs.fakestore.core.designsystem.atom.FsChip
import cl.gus.labs.fakestore.core.designsystem.atom.FsDivider
import cl.gus.labs.fakestore.core.designsystem.model.FsUiState
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
import cl.gus.labs.fakestore.core.designsystem.model.ProductDetailUiModel
import cl.gus.labs.fakestore.core.designsystem.molecule.CategoryFilterRow
import cl.gus.labs.fakestore.core.designsystem.molecule.FsDetailTopBar
import cl.gus.labs.fakestore.core.designsystem.molecule.FsListTopBar
import cl.gus.labs.fakestore.core.designsystem.molecule.FsStateBlock
import cl.gus.labs.fakestore.core.designsystem.molecule.OfflineBanner
import cl.gus.labs.fakestore.core.designsystem.organism.FsStateHost
import cl.gus.labs.fakestore.core.designsystem.organism.ProductDetailBody
import cl.gus.labs.fakestore.core.designsystem.organism.ProductDetailHeader
import cl.gus.labs.fakestore.core.designsystem.organism.ProductGrid
import cl.gus.labs.fakestore.core.designsystem.organism.ProductGridSkeleton
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import cl.gus.labs.fakestore.core.designsystem.R as DesignSystemR

private val TitleRevealOffset = 240.dp

private enum class DemoState { Loading, Content, Cache, Empty, Failure }

@Composable
fun DesignSystemCatalog(modifier: Modifier = Modifier) {
    var selectedId by remember { mutableStateOf<Int?>(null) }
    var favoriteIds by remember { mutableStateOf(setOf(5)) }
    var category by remember { mutableStateOf<String?>(null) }
    var demoState by remember { mutableStateOf(DemoState.Content) }

    val onFavoriteClick: (Int, Boolean) -> Unit = { id, checked ->
        favoriteIds = if (checked) favoriteIds + id else favoriteIds - id
    }
    val selected = SampleProducts.firstOrNull { it.id == selectedId }

    if (selected != null) {
        DetailScreen(
            product = selected,
            isFavorite = selected.id in favoriteIds,
            onFavoriteClick = { checked -> onFavoriteClick(selected.id, checked) },
            onBack = { selectedId = null },
            showBanner = demoState == DemoState.Cache,
            modifier = modifier,
        )
    } else {
        ListScreen(
            demoState = demoState,
            category = category,
            favoriteIds = favoriteIds,
            onCategorySelect = { category = it },
            onProductClick = { selectedId = it },
            onFavoriteClick = onFavoriteClick,
            onDemoStateSelect = { demoState = it },
            modifier = modifier,
        )
    }
}

@Composable
private fun ListScreen(
    demoState: DemoState,
    category: String?,
    favoriteIds: Set<Int>,
    onCategorySelect: (String?) -> Unit,
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int, Boolean) -> Unit,
    onDemoStateSelect: (DemoState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cards = SampleProducts
        .filter { category == null || it.category == category }
        .map { it.toCard() }

    val state: FsUiState<List<ProductCardUiModel>> = when (demoState) {
        DemoState.Loading -> FsUiState.Loading
        DemoState.Content -> FsUiState.Content(cards)
        DemoState.Cache -> FsUiState.Content(cards, fromCache = true)
        DemoState.Empty -> FsUiState.Empty
        DemoState.Failure -> FsUiState.Failure("Revisa tu conexión y vuelve a intentarlo.")
    }

    Column(modifier = modifier.fillMaxSize()) {
        FsListTopBar(onFavoritesClick = {})
        OfflineBanner(visible = state is FsUiState.Content && state.fromCache)
        CategoryFilterRow(
            categories = SampleCategories,
            selected = category,
            onSelect = onCategorySelect,
        )
        FsStateHost(
            state = state,
            loading = { ProductGridSkeleton(modifier = Modifier.fillMaxSize()) },
            empty = {
                CenteredBlock {
                    FsStateBlock(
                        icon = painterResource(DesignSystemR.drawable.ic_fs_box),
                        title = "Sin productos en esta categoría",
                        modifier = Modifier.fillMaxWidth(),
                        body = "Prueba con otro filtro.",
                    )
                }
            },
            failure = { message ->
                CenteredBlock {
                    FsStateBlock(
                        icon = painterResource(DesignSystemR.drawable.ic_fs_alert),
                        title = "No pudimos cargar el catálogo",
                        modifier = Modifier.fillMaxWidth(),
                        body = message,
                        iconTint = FakeStoreTheme.colors.statusError,
                        action = {
                            FsButton(
                                text = "Reintentar",
                                onClick = { onDemoStateSelect(DemoState.Content) },
                            )
                        },
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            content = { products ->
                ProductGrid(
                    products = products,
                    favoriteIds = favoriteIds,
                    onProductClick = onProductClick,
                    onFavoriteClick = onFavoriteClick,
                    modifier = Modifier.fillMaxSize(),
                )
            },
        )
        DemoStateBar(selected = demoState, onSelect = onDemoStateSelect)
    }
}

@Composable
private fun DetailScreen(
    product: ProductDetailUiModel,
    isFavorite: Boolean,
    onFavoriteClick: (Boolean) -> Unit,
    onBack: () -> Unit,
    showBanner: Boolean,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val revealPx = with(LocalDensity.current) { TitleRevealOffset.toPx() }
    val titleVisible by remember { derivedStateOf { scrollState.value > revealPx } }

    Column(modifier = modifier.fillMaxSize()) {
        FsDetailTopBar(
            title = product.title,
            titleVisible = titleVisible,
            onBackClick = onBack,
        )
        OfflineBanner(visible = showBanner)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            ProductDetailHeader(
                imageUrl = product.imageUrl,
                isFavorite = isFavorite,
                onFavoriteClick = onFavoriteClick,
            )
            ProductDetailBody(product = product)
        }
    }
}

@Composable
private fun CenteredBlock(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(FakeStoreTheme.spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun DemoStateBar(
    selected: DemoState,
    onSelect: (DemoState) -> Unit,
) {
    Column {
        FsDivider()
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(FakeStoreTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm),
        ) {
            items(DemoState.entries, key = { it.name }) { entry ->
                FsChip(
                    label = entry.name,
                    selected = entry == selected,
                    onClick = { onSelect(entry) },
                )
            }
        }
    }
}

private fun ProductDetailUiModel.toCard() = ProductCardUiModel(
    id = id,
    title = title,
    category = category,
    price = price,
    rate = rate,
    ratingCount = ratingCount,
    imageUrl = imageUrl,
)

private val SampleCategories = listOf(
    "men's clothing",
    "jewelery",
    "electronics",
    "women's clothing",
)

private val SampleProducts = listOf(
    ProductDetailUiModel(
        id = 1,
        title = "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
        category = "men's clothing",
        description = "Your perfect pack for everyday use and walks in the forest. " +
            "Stash your laptop (up to 15 inches) in the padded sleeve, your everyday.",
        price = 109.95,
        rate = 3.9,
        ratingCount = 120,
        imageUrl = "https://fakestoreapi.com/img/81fPKd-2AYL._AC_SL1500_t.png",
    ),
    ProductDetailUiModel(
        id = 5,
        title = "John Hardy Women's Legends Naga Gold & Silver Dragon Station Chain Bracelet",
        category = "jewelery",
        description = "From our Legends Collection, the Naga was inspired by the mythical " +
            "water dragon that protects the ocean's pearl.",
        price = 695.0,
        rate = 4.6,
        ratingCount = 400,
        imageUrl = "https://fakestoreapi.com/img/71pWzhdJNwL._AC_UL640_QL65_ML3_t.png",
    ),
    ProductDetailUiModel(
        id = 9,
        title = "WD 2TB Elements Portable External Hard Drive - USB 3.0",
        category = "electronics",
        description = "USB 3.0 and USB 2.0 compatibility. Fast data transfers, improved PC " +
            "performance, high capacity.",
        price = 64.0,
        rate = 3.3,
        ratingCount = 203,
        imageUrl = "https://fakestoreapi.com/img/61IBBVJvSDL._AC_SY879_t.png",
    ),
    ProductDetailUiModel(
        id = 11,
        title = "Silicon Power 256GB SSD 3D NAND A55 SLC Cache Performance Boost SATA III 2.5",
        category = "electronics",
        description = "3D NAND flash is applied to deliver high transfer speeds, enabling " +
            "faster bootup and improved overall system performance.",
        price = 109.0,
        rate = 4.8,
        ratingCount = 319,
        imageUrl = "https://fakestoreapi.com/img/71kWymZ+c+L._AC_SX679_t.png",
    ),
    ProductDetailUiModel(
        id = 15,
        title = "BIYLACLESEN Women's 3-in-1 Snowboard Jacket Winter Coats",
        category = "women's clothing",
        description = "Note: the jacket is US standard size, please choose the size you " +
            "usually wear. Material: 100% polyester, detachable warm fleece liner.",
        price = 56.99,
        rate = 2.6,
        ratingCount = 235,
        imageUrl = "https://fakestoreapi.com/img/51Y5NI-I5jL._AC_UX679_t.png",
    ),
    ProductDetailUiModel(
        id = 20,
        title = "DANVOUY Womens T Shirt Casual Cotton Short",
        category = "women's clothing",
        description = "95% cotton, 5% spandex. Casual, short sleeve, letter print, V-neck. " +
            "The fabric is soft and has some stretch.",
        price = 12.99,
        rate = 3.6,
        ratingCount = 145,
        imageUrl = "https://fakestoreapi.com/img/61pHAEJ4NML._AC_UX679_t.png",
    ),
)
