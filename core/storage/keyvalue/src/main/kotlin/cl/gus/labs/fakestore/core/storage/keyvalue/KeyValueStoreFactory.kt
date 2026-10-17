package cl.gus.labs.fakestore.core.storage.keyvalue

/** Opens the store of one owner, in a file named after it. Open each name once. */
fun interface KeyValueStoreFactory {
    fun create(name: String): KeyValueStore
}
