package cl.gus.labs.fakestore.core.storage.keyvalue.internal

import cl.gus.labs.fakestore.core.storage.keyvalue.stringKey
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private val TokenKey = stringKey("token")

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DataStoreKeyValueStoreFactoryTest {

    @get:Rule
    val tmpDir = TemporaryFolder()

    private val scope = TestScope(UnconfinedTestDispatcher())

    private fun TestScope.factory() = DataStoreKeyValueStoreFactory(backgroundScope) { name ->
        File(tmpDir.root, "$name.preferences_pb")
    }

    @Test
    fun `create keeps each name in its own file`() = scope.runTest {
        val factory = factory()
        val app = factory.create("app")
        val auth = factory.create("auth")

        app.edit { values -> values[TokenKey] = "a" }
        auth.edit { values -> values[TokenKey] = "b" }

        assertEquals("a", app.observe(TokenKey).first())
        assertEquals("b", auth.observe(TokenKey).first())
        assertTrue(File(tmpDir.root, "app.preferences_pb").isFile)
        assertTrue(File(tmpDir.root, "auth.preferences_pb").isFile)
    }

    @Test
    fun `create replaces a corrupted file, which can then be written`() = scope.runTest {
        File(tmpDir.root, "app.preferences_pb").writeText("not a proto")
        val store = factory().create("app")

        assertNull(store.observe(TokenKey).first())

        store.edit { values -> values[TokenKey] = "a" }
        assertEquals("a", store.observe(TokenKey).first())
    }

    // Learning test: one owner per store leans on this DataStore check.
    @Test
    fun `a second store on the same name fails on first use`() = scope.runTest {
        val factory = factory()
        factory.create("app").observe(TokenKey).first()

        val thrown = runCatching { factory.create("app").observe(TokenKey).first() }.exceptionOrNull()

        assertTrue("$thrown", thrown is IllegalStateException)
        assertTrue(thrown?.message, thrown?.message.orEmpty().contains("multiple DataStores"))
    }
}
