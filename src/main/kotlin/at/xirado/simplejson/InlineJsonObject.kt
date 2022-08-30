package at.xirado.simplejson

class InlineJsonObject(private val json: JSONObject): SerializableData {
    infix fun String.by(value: Any) = json.put(this, value)

    override fun toData() = json
}