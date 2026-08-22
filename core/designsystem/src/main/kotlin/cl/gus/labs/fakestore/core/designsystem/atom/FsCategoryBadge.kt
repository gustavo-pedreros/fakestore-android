package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

@Composable
fun FsCategoryBadge(
    category: String,
    modifier: Modifier = Modifier,
) {
    val color = FakeStoreTheme.colors.categoryBadge
    Box(
        modifier = modifier
            .height(24.dp)
            .border(1.dp, color, MaterialTheme.shapes.small)
            .padding(horizontal = FakeStoreTheme.spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = category.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

@PreviewLightDark
@Composable
private fun FsCategoryBadgePreview() {
    FakeStoreTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.padding(FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm),
                horizontalAlignment = Alignment.Start,
            ) {
                FsCategoryBadge("electronics")
                FsCategoryBadge("jewelery")
                FsCategoryBadge("men's clothing")
                FsCategoryBadge("women's clothing")
            }
        }
    }
}