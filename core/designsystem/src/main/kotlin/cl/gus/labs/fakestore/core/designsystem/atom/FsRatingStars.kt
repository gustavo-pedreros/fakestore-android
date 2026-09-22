package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.gus.labs.fakestore.core.designsystem.R
import cl.gus.labs.fakestore.core.designsystem.icon.FsIcons
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import java.util.Locale
import kotlin.math.floor

private const val StarCount = 5
private const val FilledAlpha = 1f
private const val PartialAlpha = 0.55f
private const val EmptyAlpha = 0.22f

internal fun formatRating(rate: Double): String = String.format(Locale.US, "%.1f", rate)

internal enum class StarFill { Filled, Partial, Empty }

internal fun starFills(rate: Double): List<StarFill> {
    val filled = floor(rate).toInt().coerceIn(0, StarCount)
    val hasPartial = filled < StarCount && rate - filled >= 0.5
    return List(StarCount) { index ->
        when {
            index < filled -> StarFill.Filled
            index == filled && hasPartial -> StarFill.Partial
            else -> StarFill.Empty
        }
    }
}

@Composable
fun FsRatingStars(
    rate: Double,
    count: Int,
    modifier: Modifier = Modifier,
    size: FsSize = FsSize.Default,
) {
    val compact = size == FsSize.Compact
    val starSize = if (compact) 13.dp else 15.dp
    val gap = if (compact) 6.dp else FakeStoreTheme.spacing.sm
    val rateText = formatRating(rate)
    val description = pluralStringResource(
        R.plurals.fs_rating_content_description,
        count,
        rateText,
        count,
    )
    val fills = starFills(rate)

    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        val star = FsIcons.Star
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(StarCount) { index ->
                Icon(
                    painter = star,
                    contentDescription = null,
                    tint = FakeStoreTheme.colors.ratingStar.copy(
                        alpha = when (fills[index]) {
                            StarFill.Filled -> FilledAlpha
                            StarFill.Partial -> PartialAlpha
                            StarFill.Empty -> EmptyAlpha
                        },
                    ),
                    modifier = Modifier.size(starSize),
                )
            }
        }
        Text(
            text = rateText,
            color = FakeStoreTheme.colors.ratingStar,
            style = FakeStoreTheme.textStyles.ratingValue.let {
                if (compact) it.copy(fontSize = 11.sp) else it
            },
        )
        Text(
            text = "($count)",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = FakeStoreTheme.textStyles.ratingCount.let {
                if (compact) it.copy(fontSize = 11.sp) else it
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewFsRatingStars() {
    FakeStoreTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.padding(FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm),
            ) {
                FsRatingStars(rate = 3.9, count = 120)
                FsRatingStars(rate = 2.1, count = 430)
                FsRatingStars(rate = 4.8, count = 679)
                FsRatingStars(rate = 1.9, count = 70)
                FsRatingStars(rate = 3.9, count = 120, size = FsSize.Compact)
            }
        }
    }
}