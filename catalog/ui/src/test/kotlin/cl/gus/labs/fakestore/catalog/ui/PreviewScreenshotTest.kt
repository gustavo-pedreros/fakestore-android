package cl.gus.labs.fakestore.catalog.ui

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziActivity
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.composeTestRule
import com.github.takahirom.roborazzi.registerRoborazziActivityToRobolectricIfNeeded
import com.github.takahirom.roborazzi.toRoborazziComposeOptions
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

// Same harness as :core:designsystem's PreviewScreenshotTest.
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
class PreviewScreenshotTest(
    private val id: String,
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `matches its baseline`() {
        preview.captureRoboImage(
            filePath = "${screenOf(preview)}/$id.png",
            roborazziComposeOptions = preview.toRoborazziComposeOptions()
                .builder()
                .composeTestRule(composeRule)
                .build(),
        )
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{0}")
        fun previews(): List<Array<Any>> = scanPreviews().map { arrayOf(idOf(it), it) }

        private fun idOf(preview: ComposablePreview<AndroidPreviewInfo>): String =
            "${preview.methodName}_${preview.previewInfo.name.ifBlank { "Default" }.replace(' ', '_')}"

        // catalog or detail: the package of the preview's file
        private fun screenOf(preview: ComposablePreview<AndroidPreviewInfo>): String =
            preview.declaringClass.substringBeforeLast('.').substringAfterLast('.')
    }
}
