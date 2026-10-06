package cl.gus.labs.fakestore.core.storage.keyvalue

/** A typed key. Its owner keeps it private; changing its type needs a new name. */
class Key<T : Any> internal constructor(val name: String, val type: ValueType<T>)

fun stringKey(name: String): Key<String> = Key(name, ValueType.StringType)

fun booleanKey(name: String): Key<Boolean> = Key(name, ValueType.BooleanType)

fun intKey(name: String): Key<Int> = Key(name, ValueType.IntType)

fun longKey(name: String): Key<Long> = Key(name, ValueType.LongType)

fun doubleKey(name: String): Key<Double> = Key(name, ValueType.DoubleType)
