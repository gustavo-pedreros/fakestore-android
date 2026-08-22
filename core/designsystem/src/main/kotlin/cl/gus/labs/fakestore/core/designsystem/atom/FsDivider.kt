package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

enum class FsDividerWeight { Hairline, Strong }

@Composable
fun FsDivider(
    modifier: Modifier = Modifier,
    weight: FsDividerWeight = FsDividerWeight.Hairline,
) {
    when (weight) {
        FsDividerWeight.Hairline -> HorizontalDivider(
            modifier = modifier,
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        FsDividerWeight.Strong -> HorizontalDivider(
            modifier = modifier,
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@PreviewLightDark
@Composable
private fun FsDividerPreview() {
    FakeStoreTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.padding(FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm),
            ) {
                FsDivider()
                Text("hairline", style = MaterialTheme.typography.labelSmall)
                FsDivider(weight = FsDividerWeight.Strong)
                Text("strong", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
