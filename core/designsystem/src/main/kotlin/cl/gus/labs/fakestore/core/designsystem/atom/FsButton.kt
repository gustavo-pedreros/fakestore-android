package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

enum class FsButtonVariant { Primary, Secondary, Tertiary }

@Composable
fun FsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FsButtonVariant = FsButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val shape = MaterialTheme.shapes.medium
    val container: Color = when {
        variant != FsButtonVariant.Primary -> Color.Transparent
        enabled -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val content: Color = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
        variant == FsButtonVariant.Primary -> MaterialTheme.colorScheme.background
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderWidth: Dp = if (variant == FsButtonVariant.Secondary) 2.dp else 0.dp
    val borderColor: Color = when {
        variant != FsButtonVariant.Secondary -> Color.Transparent
        enabled -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val horizontalPadding = if (variant == FsButtonVariant.Tertiary) 12.dp else 22.dp
    val textStyle = MaterialTheme.typography.labelLarge.let {
        if (variant == FsButtonVariant.Tertiary) it.copy(textDecoration = TextDecoration.Underline) else it
    }

    Row(
        modifier = modifier
            .height(48.dp)
            .background(container, shape)
            .border(borderWidth, borderColor, shape)
            .clip(shape)
            .clickable(
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = content,
                strokeWidth = 2.dp,
            )
        }
        Text(
            text = text.uppercase(),
            color = content,
            style = textStyle,
            maxLines = 1,
        )
    }
}

@PreviewLightDark
@Composable
private fun FsButtonPreview() {
    FakeStoreTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.padding(FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.md),
                horizontalAlignment = Alignment.Start,
            ) {
                FsButton("Reintentar", onClick = {})
                FsButton("Ver todo", onClick = {}, variant = FsButtonVariant.Secondary)
                FsButton("Descartar", onClick = {}, variant = FsButtonVariant.Tertiary)
                FsButton("Deshabilitado", onClick = {}, enabled = false)
                FsButton("Cargando", onClick = {}, loading = true)
            }
        }
    }
}
