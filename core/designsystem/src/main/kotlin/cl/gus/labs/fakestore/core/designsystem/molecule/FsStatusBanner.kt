package cl.gus.labs.fakestore.core.designsystem.molecule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import cl.gus.labs.fakestore.core.designsystem.R
import cl.gus.labs.fakestore.core.designsystem.icon.FsIcons
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

private val BannerHeight = 40.dp
private val IconSize = 17.dp
private val ContentGap = 10.dp

@Composable
fun FsStatusBanner(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    icon: Painter = FsIcons.WifiOff,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        val colors = FakeStoreTheme.colors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BannerHeight)
                .background(colors.offlineBg)
                .semantics { liveRegion = LiveRegionMode.Polite }
                .padding(horizontal = FakeStoreTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ContentGap),
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = colors.offlineText,
                modifier = Modifier.size(IconSize),
            )
            Text(
                text = message.uppercase(),
                color = colors.offlineText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 0.12f.em,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun FsStatusBannerPreview() {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(vertical = FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.lg),
            ) {
                FsStatusBanner(
                    visible = true,
                    message = stringResource(R.string.fs_offline_banner),
                )
                FsStatusBanner(
                    visible = true,
                    message = "Datos del 22 de agosto, 14:30",
                )
                FsStatusBanner(visible = false, message = "")
            }
        }
    }
}
