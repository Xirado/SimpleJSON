import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.JSONObject

inline fun <reified T> JSONArray.collect() = IntRange(0, length() - 1).map { get<T>(it) }

inline fun <reified T> JSONArray.get(index: Int) = when (T::class) {
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