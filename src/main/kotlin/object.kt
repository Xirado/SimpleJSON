import at.xirado.simplejson.JSONArray
import at.xirado.simplejson.JSONObject

@Suppress("UNCHECKED_CAST")
inline fun <reified T> JSONArray.asSequence(): Sequence<T> = when (T::class) {
    String::class -> IntRange(0, length()).map { getString(it) }.asSequence() as Sequence<T>
    Int::class -> IntRange(0, length()).map { getInt(it) }.asSequence() as Sequence<T>
    Long::class -> IntRange(0, length()).map { getLong(it) }.asSequence() as Sequence<T>
    Double::class -> IntRange(0, length()).map { getDouble(it) }.asSequence() as Sequence<T>
    Boolean::class -> IntRange(0, length()).map { getBoolean(it) }.asSequence() as Sequence<T>
    JSONObject::class -> IntRange(0, length()).map { getObject(it) }.asSequence() as Sequence<T>
    JSONArray::class -> IntRange(0, length()).map { getObject(it) }.asSequence() as Sequence<T>
    else -> throw IllegalArgumentException("Cannot get sequence of that type!")
}