package cl.gus.labs.fakestore.core.designsystem.molecule

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.gus.labs.fakestore.core.designsystem.R
import cl.gus.labs.fakestore.core.designsystem.atom.FsButton
import cl.gus.labs.fakestore.core.designsystem.atom.FsButtonVariant
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

private val IconSize = 34.dp
private val ContentGap = 14.dp
private val ActionGap = 4.dp

@Composable
fun FsStateBlock(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    action: (@Composable () -> Unit)? = null,
) {
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(
                horizontal = FakeStoreTheme.spacing.xl,
                vertical = FakeStoreTheme.spacing.xxl,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ContentGap),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(IconSize),
        )
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                lineHeight = 23.sp,
            ),
            textAlign = TextAlign.Center,
        )
        if (body != null) {
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        if (action != null) {
            Box(modifier = Modifier.padding(top = ActionGap)) {
                action()
            }
        }
    }
}

@Preview(name = "Light", heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FsStateBlockPreview() {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.lg),
            ) {
                FsStateBlock(
                    icon = painterResource(R.drawable.ic_fs_alert),
                    title = "No pudimos cargar el catálogo",
                    modifier = Modifier.fillMaxWidth(),
                    body = "Revisa tu conexión y vuelve a intentarlo.",
                    iconTint = FakeStoreTheme.colors.statusError,
                    action = { FsButton("Reintentar", onClick = {}) },
                )
                FsStateBlock(
                    icon = painterResource(R.drawable.ic_fs_box),
                    title = "Sin productos en esta categoría",
                    modifier = Modifier.fillMaxWidth(),
                    body = "Prueba con otro filtro.",
                    action = {
                        FsButton("Ver todo", onClick = {}, variant = FsButtonVariant.Secondary)
                    },
                )
                FsStateBlock(
                    icon = painterResource(R.drawable.ic_fs_heart_outline),
                    title = "Aún no tienes favoritos",
                    modifier = Modifier.fillMaxWidth(),
                    body = "Toca el corazón en cualquier producto para guardarlo aquí.",
                )
            }
        }
    }
}
