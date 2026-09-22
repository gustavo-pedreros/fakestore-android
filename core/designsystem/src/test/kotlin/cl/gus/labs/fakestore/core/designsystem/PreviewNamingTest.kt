package cl.gus.labs.fakestore.core.designsystem

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Previews")
class PreviewNamingTest {

    @Test
    @DisplayName("start with Preview, so Kover's @Preview* filter cannot hide the composable they preview")
    fun startWithPreview() {
        val previews = scanPreviews()
        assertFalse(previews.isEmpty(), "the scan found no previews")

        val misnamed = previews.map { it.methodName }.distinct().filterNot { it.startsWith("Preview") }

        // FooPreview is a prefix match for Foo's lambdas, so the filter drops those from the report too
        assertEquals(emptyList<String>(), misnamed, "rename these to Preview<Name>")
    }
}
