package cl.gus.labs.fakestore.core.storage.keyvalue.internal

import cl.gus.labs.fakestore.core.storage.keyvalue.stringKey
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

private val TokenKey = stringKey("token")

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("DataStoreKeyValueStoreFactory")
class DataStoreKeyValueStoreFactoryTest {

    @TempDir
    lateinit var tmpDir: File

    private val scope = TestScope(UnconfinedTestDispatcher())

    private fun TestScope.factory() = DataStoreKeyValueStoreFactory(backgroundScope) { name ->
        File(tmpDir, "$name.preferences_pb")
    }

    @Test
    @DisplayName("keeps each name in its own file")
    fun keepsNamesApart() = scope.runTest {
        val factory = factory()
        val app = factory.create("app")
        val auth = factory.create("auth")

        app.edit { values -> values[TokenKey] = "a" }
        auth.edit { values -> values[TokenKey] = "b" }

        assertEquals("a", app.observe(TokenKey).first())
        assertEquals("b", auth.observe(TokenKey).first())
        assertTrue(File(tmpDir, "app.preferences_pb").isFile)
        assertTrue(File(tmpDir, "auth.preferences_pb").isFile)
    }

    @Test
    @DisplayName("replaces a corrupted file, which can then be written")
    fun replacesACorruptedFile() = scope.runTest {
        File(tmpDir, "app.preferences_pb").writeText("not a proto")
        val store = factory().create("app")

        assertNull(store.observe(TokenKey).first())

        store.edit { values -> values[TokenKey] = "a" }
        assertEquals("a", store.observe(TokenKey).first())
    }

    // Learning test: one owner per store leans on this DataStore check.
    @Test
    @DisplayName("fails the first use of a second store on the same name")
    fun secondStoreOnTheSameNameFails() = scope.runTest {
        val factory = factory()
        factory.create("app").observe(TokenKey).first()

        val thrown = runCatching { factory.create("app").observe(TokenKey).first() }.exceptionOrNull()

        assertInstanceOf(IllegalStateException::class.java, thrown)
        assertTrue(thrown?.message.orEmpty().contains("multiple DataStores"), thrown?.message)
    }
}
