package cl.gus.labs.fakestore.catalog.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cl.gus.labs.fakestore.catalog.ui.R
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant
import java.time.Instant as JavaInstant

private val StaleFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

@Composable
internal fun staleMessage(lastSyncedAt: Instant?): String {
    if (lastSyncedAt == null) return stringResource(R.string.catalog_stale_banner_unknown)
    val formatted = remember(lastSyncedAt) {
        StaleFormatter
            .withZone(ZoneId.systemDefault())
            .format(JavaInstant.ofEpochMilli(lastSyncedAt.toEpochMilliseconds()))
    }
    return stringResource(R.string.catalog_stale_banner, formatted)
}

@Composable
internal fun CenteredBlock(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(FakeStoreTheme.spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
