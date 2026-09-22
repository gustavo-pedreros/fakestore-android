package cl.gus.labs.fakestore.core.designsystem

import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

// One entry per @Preview variant, so @PreviewLightDark counts twice. The previews are private functions
// at the bottom of their component's file, hence includePrivatePreviews.
internal fun scanPreviews(): List<ComposablePreview<AndroidPreviewInfo>> =
    AndroidComposablePreviewScanner()
        .scanPackageTrees("cl.gus.labs.fakestore.core.designsystem")
        .includePrivatePreviews()
        .getPreviews()
