package at.xirado.simplejson

inline fun <reified T> JSONObject.get(key: String) = when (T::class) {
    String::class -> getString(key) as T
    Int::class -> getInt(key) as T
    Long::class -> getLong(key) as T
    Double::class -> getDouble(key) as T
    Float::class -> getDouble(key).toFloat() as T
    Boolean::class -> getBoolean(key) as T
    JSONObject::class -> getObject(key) as T
    JSONArray::class -> getArray(key) as T
    else -> throw IllegalArgumentException("Cannot get object of type ${T::class.simpleName}")
}

inline fun json(block: InlineJsonObject.() -> Unit) = InlineJsonObject(JSONObject.empty()).apply(block).toData()



