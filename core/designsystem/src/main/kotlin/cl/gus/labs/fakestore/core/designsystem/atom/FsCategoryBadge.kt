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
import androidx.compose.ui.unit.sp
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

@Composable
fun FsCategoryBadge(
    category: String,
    modifier: Modifier = Modifier,
    size: FsSize = FsSize.Default,
) {
    val color = FakeStoreTheme.colors.categoryBadge
    val height = if (size == FsSize.Compact) 20.dp else 24.dp
    val horizontalPadding = if (size == FsSize.Compact) 6.dp else FakeStoreTheme.spacing.sm
    val style = MaterialTheme.typography.labelSmall.let {
        if (size == FsSize.Compact) it.copy(fontSize = 9.sp) else it
    }
    Box(
        modifier = modifier
            .height(height)
            .border(1.dp, color, MaterialTheme.shapes.small)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = category.uppercase(),
            color = color,
            style = style,
            maxLines = 1,
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewFsCategoryBadge() {
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
                FsCategoryBadge("women's clothing", size = FsSize.Compact)
            }
        }
    }
}