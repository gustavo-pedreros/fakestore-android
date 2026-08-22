package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.R
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

private const val SavingAlpha = 0.5f

@Composable
fun FsFavoriteButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    saving: Boolean = false,
) {
    val colors = FakeStoreTheme.colors
    val discColor = when {
        saving -> MaterialTheme.colorScheme.surfaceContainer
        checked -> colors.favoriteOnBg
        else -> colors.imageTile
    }
    val discBorder = when {
        saving -> Color.Transparent
        checked -> colors.favoriteOnGlyph
        else -> colors.imageTileBorder
    }
    val glyphColor = when {
        saving -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SavingAlpha)
        checked -> colors.favoriteOnGlyph
        else -> colors.favoriteOffGlyph
    }
    val glyph = if (checked && !saving) R.drawable.ic_fs_heart_filled else R.drawable.ic_fs_heart_outline

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .toggleable(
                value = checked,
                enabled = !saving,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(discColor, FakeStoreTheme.favoriteDiscShape)
                .border(1.dp, discBorder, FakeStoreTheme.favoriteDiscShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(glyph),
                contentDescription = contentDescription,
                tint = glyphColor,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun FsFavoriteButtonPreview() {
    FakeStoreTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Row(
                modifier = Modifier.padding(FakeStoreTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.xl),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FsFavoriteButton(
                    checked = false,
                    onCheckedChange = {},
                    contentDescription = "Añadir a favoritos",
                )
                FsFavoriteButton(
                    checked = true,
                    onCheckedChange = {},
                    contentDescription = "Quitar de favoritos",
                )
                FsFavoriteButton(
                    checked = false,
                    onCheckedChange = {},
                    contentDescription = "Guardando favorito",
                    saving = true,
                )
            }
        }
    }
}
