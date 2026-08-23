package cl.gus.labs.fakestore.core.designsystem.molecule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.R
import cl.gus.labs.fakestore.core.designsystem.atom.FsDivider
import cl.gus.labs.fakestore.core.designsystem.icon.FsIcons
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

private val BarHeight = 60.dp
private val ActionSize = 48.dp
private val GlyphSize = 22.dp
private val ActionGap = 4.dp
private const val Wordmark = "FAKE\nSTORE"
private const val WordmarkLabel = "FakeStore"

@Composable
fun FsListTopBar(
    onFavoritesClick: () -> Unit,
    modifier: Modifier = Modifier,
    onThemeToggleClick: (() -> Unit)? = null,
) {
    TopBarSurface(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FakeStoreTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = Wordmark,
                color = MaterialTheme.colorScheme.onSurface,
                style = FakeStoreTheme.textStyles.wordmark,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = WordmarkLabel
                },
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(ActionGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TopBarAction(
                    icon = FsIcons.HeartOutline,
                    contentDescription = stringResource(R.string.fs_action_favorites),
                    onClick = onFavoritesClick,
                )
                if (onThemeToggleClick != null) {
                    TopBarAction(
                        icon = FsIcons.Moon,
                        contentDescription = stringResource(R.string.fs_action_theme),
                        onClick = onThemeToggleClick,
                    )
                }
            }
        }
    }
}

@Composable
fun FsDetailTopBar(
    title: String,
    titleVisible: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onShareClick: (() -> Unit)? = null,
) {
    TopBarSurface(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = FakeStoreTheme.spacing.xs,
                    end = FakeStoreTheme.spacing.sm,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ActionGap),
        ) {
            TopBarAction(
                icon = FsIcons.ArrowBack,
                contentDescription = stringResource(R.string.fs_action_back),
                onClick = onBackClick,
            )
            DetailTitle(
                title = title,
                visible = titleVisible,
                modifier = Modifier.weight(1f),
            )
            if (onShareClick != null) {
                TopBarAction(
                    icon = FsIcons.Share,
                    contentDescription = stringResource(R.string.fs_action_share),
                    onClick = onShareClick,
                )
            } else {
                Spacer(Modifier.width(FakeStoreTheme.spacing.sm))
            }
        }
    }
}

@Composable
private fun DetailTitle(
    title: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TopBarSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight),
        ) {
            content()
        }
        FsDivider()
    }
}

@Composable
private fun TopBarAction(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(ActionSize)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(GlyphSize),
        )
    }
}

@PreviewLightDark
@Composable
private fun FsTopBarPreview() {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(vertical = FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.lg),
            ) {
                FsListTopBar(onFavoritesClick = {})
                FsListTopBar(onFavoritesClick = {}, onThemeToggleClick = {})
                FsDetailTopBar(
                    title = "Fjallraven Foldsack No. 1",
                    titleVisible = true,
                    onBackClick = {},
                    onShareClick = {},
                )
                FsDetailTopBar(
                    title = "Fjallraven Foldsack No. 1",
                    titleVisible = false,
                    onBackClick = {},
                    onShareClick = {},
                )
            }
        }
    }
}
