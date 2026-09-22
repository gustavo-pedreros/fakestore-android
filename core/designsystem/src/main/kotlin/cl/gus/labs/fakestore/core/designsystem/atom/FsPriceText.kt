package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import java.util.Locale

enum class FsPriceSize { Large, Medium }

@Composable
fun FsPriceText(
    amount: Double,
    modifier: Modifier = Modifier,
    size: FsPriceSize = FsPriceSize.Medium,
) {
    val style = when (size) {
        FsPriceSize.Large -> FakeStoreTheme.textStyles.priceLarge
        FsPriceSize.Medium -> FakeStoreTheme.textStyles.priceMedium
    }
    Text(
        text = String.format(Locale.US, "$%.2f", amount),
        modifier = modifier,
        color = FakeStoreTheme.colors.priceText,
        style = style,
        maxLines = 1,
    )
}

@PreviewLightDark
@Composable
private fun PreviewFsPriceText() {
    FakeStoreTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.padding(FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm),
            ) {
                FsPriceText(109.95, size = FsPriceSize.Large)
                FsPriceText(695.0)
                FsPriceText(15.99)
            }
        }
    }
}
