package at.xirado.simplejson

inline fun <reified T> JSONArray.collect() = (0 until length()).map { getValue<T>(it) }

inline fun <reified T> JSONArray.asSequence() = (0 until length()).asSequence().map { getValue<T>(it) }

inline fun <reified T> JSONArray.getValue(index: Int) = when (T::class) {
    String::class -> getString(index) as T
    Int::class -> getInt(index) as T
    Long::class -> getLong(index) as T
    Double::class -> getDouble(index) as T
    Float::class -> getDouble(index).toFloat() as T
    Boolean::class -> getBoolean(index) as T
    JSONObject::class -> getObject(index) as T
    JSONArray::class -> getArray(index) as T
    else -> throw IllegalArgumentException("Cannot get object of type ${T::class.simpleName}")
}

inline fun <reified T> JSONArray.get(index: Int): T? {
    return if (isNull(index))
        null
    else
        getValue(index)
}