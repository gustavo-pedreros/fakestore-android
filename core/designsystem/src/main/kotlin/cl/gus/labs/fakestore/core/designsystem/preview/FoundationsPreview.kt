package cl.gus.labs.fakestore.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

@Preview(name = "Light", heightDp = 1600, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", heightDp = 1600, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FoundationsPreview() {
    FakeStoreTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.xxl),
            ) {
                ColorSection()
                TypeSection()
                SpacingSection()
            }
        }
    }
}

@Composable
private fun ColorSection() {
    Column(verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm)) {
        Text("Material 3", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm)) {
            ColorSwatch("background", MaterialTheme.colorScheme.background)
            ColorSwatch("surface", MaterialTheme.colorScheme.surface)
            ColorSwatch("surfaceContainer", MaterialTheme.colorScheme.surfaceContainer)
            ColorSwatch("surfaceContainerHigh", MaterialTheme.colorScheme.surfaceContainerHigh)
            ColorSwatch("onSurface", MaterialTheme.colorScheme.onSurface)
            ColorSwatch("onSurfaceVariant", MaterialTheme.colorScheme.onSurfaceVariant)
            ColorSwatch("primary", MaterialTheme.colorScheme.primary)
            ColorSwatch("onPrimary", MaterialTheme.colorScheme.onPrimary)
            ColorSwatch("outlineVariant", MaterialTheme.colorScheme.outlineVariant)
            ColorSwatch("outline", MaterialTheme.colorScheme.outline)
            ColorSwatch("error", MaterialTheme.colorScheme.error)
            ColorSwatch("onError", MaterialTheme.colorScheme.onError)
            ColorSwatch("inverseSurface", MaterialTheme.colorScheme.inverseSurface)
            ColorSwatch("inverseOnSurface", MaterialTheme.colorScheme.inverseOnSurface)
        }

        Text("Semántica", style = MaterialTheme.typography.titleMedium)
        val colors = FakeStoreTheme.colors
        FlowRow(horizontalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm)) {
            ColorSwatch("imageTile", colors.imageTile)
            ColorSwatch("imageTileBorder", colors.imageTileBorder)
            ColorSwatch("priceText", colors.priceText)
            ColorSwatch("favoriteOnBg", colors.favoriteOnBg)
            ColorSwatch("favoriteOnGlyph", colors.favoriteOnGlyph)
            ColorSwatch("favoriteOffGlyph", colors.favoriteOffGlyph)
            ColorSwatch("offlineBg", colors.offlineBg)
            ColorSwatch("offlineText", colors.offlineText)
            ColorSwatch("categoryBadge", colors.categoryBadge)
            ColorSwatch("skeleton", colors.skeleton)
            ColorSwatch("ratingStar", colors.ratingStar)
            ColorSwatch("statusError", colors.statusError)
        }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color, MaterialTheme.shapes.small)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small),
        )
        Text(name, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TypeSection() {
    Column(verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm)) {
        Text("Tipografía", style = MaterialTheme.typography.titleMedium)
        Text("Display Large", style = MaterialTheme.typography.displayLarge)
        Text("Headline Medium", style = MaterialTheme.typography.headlineMedium)
        Text("Title Large", style = MaterialTheme.typography.titleLarge)
        Text("Title Medium", style = MaterialTheme.typography.titleMedium)
        Text("Body Large", style = MaterialTheme.typography.bodyLarge)
        Text("Body Medium", style = MaterialTheme.typography.bodyMedium)
        Text("Reintentar".uppercase(), style = MaterialTheme.typography.labelLarge)
        Text("Electronics".uppercase(), style = MaterialTheme.typography.labelSmall)
        Text(
            "$599.90",
            style = FakeStoreTheme.textStyles.priceLarge,
            color = FakeStoreTheme.colors.priceText,
        )
        Text(
            "$29.90",
            style = FakeStoreTheme.textStyles.priceMedium,
            color = FakeStoreTheme.colors.priceText,
        )
    }
}

@Composable
private fun SpacingSection() {
    Column(verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm)) {
        Text("Espaciado", style = MaterialTheme.typography.titleMedium)
        SpacingBar("xs", FakeStoreTheme.spacing.xs)
        SpacingBar("sm", FakeStoreTheme.spacing.sm)
        SpacingBar("md", FakeStoreTheme.spacing.md)
        SpacingBar("lg", FakeStoreTheme.spacing.lg)
        SpacingBar("xl", FakeStoreTheme.spacing.xl)
        SpacingBar("xxl", FakeStoreTheme.spacing.xxl)
        SpacingBar("huge", FakeStoreTheme.spacing.huge)
    }
}

@Composable
private fun SpacingBar(label: String, value: Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(40.dp),
        )
        Box(
            modifier = Modifier
                .height(14.dp)
                .width(value)
                .background(MaterialTheme.colorScheme.onSurface),
        )
        Text("${value.value.toInt()}dp", style = MaterialTheme.typography.bodyMedium)
    }
}
