package cl.gus.labs.fakestore.core.storage.keyvalue.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.gus.labs.fakestore.core.storage.keyvalue.Key
import cl.gus.labs.fakestore.core.storage.keyvalue.KeyValueStore
import cl.gus.labs.fakestore.core.storage.keyvalue.ValueType
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class DataStoreKeyValueStore(
    private val dataStore: DataStore<Preferences>,
) : KeyValueStore {

    private val preferences: Flow<Preferences> = dataStore.data
        .catch { cause ->
            if (cause is IOException) emit(emptyPreferences()) else throw cause
        }

    override fun <T : Any> observe(key: Key<T>): Flow<T?> {
        val preferencesKey = key.toPreferencesKey()
        return preferences
            .map { preferences -> key.type.cast(preferences[preferencesKey]) }
            .distinctUntilChanged()
    }

    override suspend fun edit(transaction: (KeyValueStore.Editor) -> Unit) {
        dataStore.edit { preferences -> transaction(PreferencesEditor(preferences)) }
    }
}

private class PreferencesEditor(
    private val preferences: MutablePreferences,
) : KeyValueStore.Editor {

    override fun <T : Any> get(key: Key<T>): T? = key.type.cast(preferences[key.toPreferencesKey()])

    override fun <T : Any> set(key: Key<T>, value: T?) {
        val preferencesKey = key.toPreferencesKey()
        if (value == null) preferences.remove(preferencesKey) else preferences[preferencesKey] = value
    }
}

// Exhaustive over the sealed ValueType, so each branch fixes T and the cast holds.
@Suppress("UNCHECKED_CAST")
private fun <T : Any> Key<T>.toPreferencesKey(): Preferences.Key<T> = when (type) {
    ValueType.StringType -> stringPreferencesKey(name)
    ValueType.BooleanType -> booleanPreferencesKey(name)
    ValueType.IntType -> intPreferencesKey(name)
    ValueType.LongType -> longPreferencesKey(name)
    ValueType.DoubleType -> doublePreferencesKey(name)
} as Preferences.Key<T>
