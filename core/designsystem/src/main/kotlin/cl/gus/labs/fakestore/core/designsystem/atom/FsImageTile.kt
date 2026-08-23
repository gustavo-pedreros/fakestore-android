package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.R
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import cl.gus.labs.fakestore.core.designsystem.theme.StoneSoft
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter

private val MissingGlyphSize = 26.dp

@Composable
fun FsImageTile(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    ratio: Float = 1f,
    contentPadding: Dp = 14.dp,
    showBorder: Boolean = true,
) {
    val colors = FakeStoreTheme.colors
    val painter = rememberAsyncImagePainter(model = url)
    val state by painter.state.collectAsState()
    val loading = state is AsyncImagePainter.State.Loading

    Box(
        modifier = modifier
            .aspectRatio(ratio)
            .background(if (loading) colors.skeleton else colors.imageTile)
            .then(
                if (showBorder) Modifier.border(1.dp, colors.imageTileBorder) else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            is AsyncImagePainter.State.Loading -> Unit

            is AsyncImagePainter.State.Success -> Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentScale = ContentScale.Fit,
            )

            is AsyncImagePainter.State.Error,
            is AsyncImagePainter.State.Empty,
            -> Icon(
                painter = painterResource(R.drawable.ic_fs_image_missing),
                contentDescription = contentDescription,
                tint = StoneSoft,
                modifier = Modifier.size(MissingGlyphSize),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun FsImageTilePreview() {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(FakeStoreTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm),
            ) {
                FsImageTile(
                    url = null,
                    contentDescription = null,
                    modifier = Modifier.width(120.dp),
                )
                FsImageTile(
                    url = null,
                    contentDescription = null,
                    modifier = Modifier.width(120.dp),
                    ratio = 4f / 3f,
                )
            }
        }
    }
}
