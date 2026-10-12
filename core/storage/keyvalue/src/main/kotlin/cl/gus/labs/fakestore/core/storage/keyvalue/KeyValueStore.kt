package cl.gus.labs.fakestore.core.storage.keyvalue

import kotlinx.coroutines.flow.Flow

interface KeyValueStore {

    /** Null when the key is absent, holds another type, or the store cannot be read. */
    fun <T : Any> observe(key: Key<T>): Flow<T?>

    /** One atomic write. Throws IOException when it fails; writes nothing when [transaction] throws. */
    suspend fun edit(transaction: (Editor) -> Unit)

    interface Editor {
        operator fun <T : Any> get(key: Key<T>): T?

        /** A null value removes the key. */
        operator fun <T : Any> set(key: Key<T>, value: T?)
    }
}

/** Reads and writes one key in one transaction. */
suspend fun <T : Any> KeyValueStore.update(key: Key<T>, transform: (T?) -> T?) {
    edit { values -> values[key] = transform(values[key]) }
}
