package cl.gus.labs.fakestore.core.designsystem.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.R
import cl.gus.labs.fakestore.core.designsystem.atom.FsChip
import cl.gus.labs.fakestore.core.designsystem.atom.FsDivider
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

private val RowHeight = 68.dp
private const val AllKey = "fs_category_all"

@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(RowHeight),
            contentPadding = PaddingValues(horizontal = FakeStoreTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item(key = AllKey) {
                FsChip(
                    label = stringResource(R.string.fs_category_all),
                    selected = selected == null,
                    onClick = { onSelect(null) },
                )
            }
            items(categories, key = { it }) { category ->
                FsChip(
                    label = category,
                    selected = category == selected,
                    onClick = { onSelect(category) },
                )
            }
        }
        FsDivider()
    }
}

@PreviewLightDark
@Composable
private fun CategoryFilterRowPreview() {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(vertical = FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.lg),
            ) {
                CategoryFilterRow(
                    categories = SampleCategories,
                    selected = null,
                    onSelect = {},
                )
                CategoryFilterRow(
                    categories = SampleCategories,
                    selected = "electronics",
                    onSelect = {},
                )
            }
        }
    }
}

private val SampleCategories = listOf(
    "jewelery",
    "electronics",
    "men's clothing",
    "women's clothing",
)
