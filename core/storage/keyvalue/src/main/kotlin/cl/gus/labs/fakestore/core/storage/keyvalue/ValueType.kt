package cl.gus.labs.fakestore.core.storage.keyvalue

/** The value types every store keeps natively, on any mechanism. */
sealed interface ValueType<T : Any> {

    /** [value] as this type; null when it is absent or of another type. */
    fun cast(value: Any?): T?

    data object StringType : ValueType<String> {
        override fun cast(value: Any?): String? = value as? String
    }

    data object BooleanType : ValueType<Boolean> {
        override fun cast(value: Any?): Boolean? = value as? Boolean
    }

    data object IntType : ValueType<Int> {
        override fun cast(value: Any?): Int? = value as? Int
    }

    data object LongType : ValueType<Long> {
        override fun cast(value: Any?): Long? = value as? Long
    }

    data object DoubleType : ValueType<Double> {
        override fun cast(value: Any?): Double? = value as? Double
    }
}
