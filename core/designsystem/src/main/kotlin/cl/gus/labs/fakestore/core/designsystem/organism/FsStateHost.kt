package cl.gus.labs.fakestore.core.designsystem.organism

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.atom.FsButton
import cl.gus.labs.fakestore.core.designsystem.icon.FsIcons
import cl.gus.labs.fakestore.core.designsystem.model.FsUiState
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
import cl.gus.labs.fakestore.core.designsystem.molecule.FsStateBlock
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

@Composable
fun <T> FsStateHost(
    state: FsUiState<T>,
    loading: @Composable () -> Unit,
    empty: @Composable () -> Unit,
    failure: @Composable (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    Box(modifier = modifier) {
        when (state) {
            FsUiState.Loading -> loading()
            FsUiState.Empty -> empty()
            is FsUiState.Failure -> failure(state.message)
            is FsUiState.Content -> content(state.data)
        }
    }
}

private val PreviewStateHeight = 210.dp

private val SampleProducts = listOf(
    ProductCardUiModel(
        id = 1,
        title = "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
        category = "men's clothing",
        price = 109.95,
        rate = 3.9,
        ratingCount = 120,
        imageUrl = null,
    ),
    ProductCardUiModel(
        id = 2,
        title = "Mens Casual Premium Slim Fit T-Shirts",
        category = "men's clothing",
        price = 22.3,
        rate = 4.1,
        ratingCount = 259,
        imageUrl = null,
    ),
)

@Composable
private fun StateSample(
    label: String,
    state: FsUiState<List<ProductCardUiModel>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
        FsStateHost(
            state = state,
            loading = { ProductGridSkeleton(modifier = Modifier.fillMaxSize()) },
            empty = {
                FsStateBlock(
                    icon = FsIcons.Box,
                    title = "Sin productos en esta categoría",
                    modifier = Modifier.fillMaxWidth(),
                    body = "Prueba con otro filtro.",
                )
            },
            failure = { message ->
                FsStateBlock(
                    icon = FsIcons.Alert,
                    title = "No pudimos cargar el catálogo",
                    modifier = Modifier.fillMaxWidth(),
                    body = message,
                    iconTint = FakeStoreTheme.colors.statusError,
                    action = { FsButton("Reintentar", onClick = {}) },
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(PreviewStateHeight),
            content = { products ->
                ProductGrid(
                    products = products,
                    favoriteIds = setOf(2),
                    onProductClick = {},
                    onFavoriteClick = { _, _ -> },
                    modifier = Modifier.fillMaxSize(),
                )
            },
        )
    }
}

@Preview(name = "Light", heightDp = 1060, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", heightDp = 1060, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewFsStateHost() {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.lg),
                horizontalAlignment = Alignment.Start,
            ) {
                StateSample("LOADING", FsUiState.Loading)
                StateSample("CONTENT", FsUiState.Content(SampleProducts, fromCache = true))
                StateSample("EMPTY", FsUiState.Empty)
                StateSample(
                    "FAILURE",
                    FsUiState.Failure("Revisa tu conexión y vuelve a intentarlo."),
                )
            }
        }
    }
}
