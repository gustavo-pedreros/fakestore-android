package cl.gus.labs.fakestore.core.designsystem

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

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
class PreviewScreenshotTest(
    private val id: String,
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {

    // Robolectric has to know the activity before the compose rule launches it
    @get:Rule(order = 0)
    val registerActivity = object : TestWatcher() {
        override fun starting(description: Description) = registerRoborazziActivityToRobolectricIfNeeded()
    }

    // The rule's test clock cancels infinite animations (the loading spinner, the skeleton pulse); on the
    // real clock they keep the main looper from ever going idle and the capture never returns
    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<RoborazziActivity>()

    @Test
    fun `matches its baseline`() {
        preview.captureRoboImage(
            filePath = "${layerOf(preview)}/$id.png",
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

        // The baseline's file name: stable when a preview changes size or gains a sibling, so the review
        // shows an image diff instead of a delete and an add
        private fun idOf(preview: ComposablePreview<AndroidPreviewInfo>): String =
            "${preview.methodName}_${preview.previewInfo.name.ifBlank { "Default" }.replace(' ', '_')}"

        // atom, molecule, organism or preview: the package the preview's file lives in
        private fun layerOf(preview: ComposablePreview<AndroidPreviewInfo>): String =
            preview.declaringClass.substringBeforeLast('.').substringAfterLast('.')
    }
}
