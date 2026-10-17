package cl.gus.labs.fakestore.core.storage.keyvalue.internal

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import cl.gus.labs.fakestore.core.storage.keyvalue.KeyValueStore
import cl.gus.labs.fakestore.core.storage.keyvalue.KeyValueStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope

internal class DataStoreKeyValueStoreFactory(
    private val scope: CoroutineScope,
    private val produceFile: (name: String) -> File,
) : KeyValueStoreFactory {

    // A corrupted file is replaced, so its owner falls back to its defaults and can write again.
    override fun create(name: String): KeyValueStore = DataStoreKeyValueStore(
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            scope = scope,
            produceFile = { produceFile(name) },
        ),
    )
}
