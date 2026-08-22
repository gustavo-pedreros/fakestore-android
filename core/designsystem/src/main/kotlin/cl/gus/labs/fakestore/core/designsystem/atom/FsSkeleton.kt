package cl.gus.labs.fakestore.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

@Composable
fun FsSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    Box(modifier.background(FakeStoreTheme.colors.skeleton, shape))
}

@PreviewLightDark
@Composable
private fun FsSkeletonPreview() {
    FakeStoreTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.padding(FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm),
            ) {
                FsSkeleton(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                )
                FsSkeleton(
                    Modifier
                        .fillMaxWidth(0.82f)
                        .height(12.dp),
                )
                FsSkeleton(
                    Modifier
                        .fillMaxWidth(0.46f)
                        .height(12.dp),
                )
            }
        }
    }
}