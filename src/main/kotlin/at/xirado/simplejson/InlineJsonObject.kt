package at.xirado.simplejson

class InlineJsonObject(private val json: JSONObject) : SerializableData {
    infix fun String.by(value: Any) = json.put(this, value)

    infix fun String.by(builder: InlineJsonObject.() -> Unit) =
        json.put(this, InlineJsonObject(JSONObject.empty()).apply(builder))

    override fun toData() = json
}