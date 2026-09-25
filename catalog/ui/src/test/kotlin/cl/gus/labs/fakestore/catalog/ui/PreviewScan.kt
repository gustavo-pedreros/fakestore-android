package cl.gus.labs.fakestore.catalog.ui

import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

// One entry per variant: @PreviewLightDark counts twice.
internal fun scanPreviews(): List<ComposablePreview<AndroidPreviewInfo>> =
    AndroidComposablePreviewScanner()
        .scanPackageTrees("cl.gus.labs.fakestore.catalog.ui")
        .includePrivatePreviews()
        .getPreviews()
