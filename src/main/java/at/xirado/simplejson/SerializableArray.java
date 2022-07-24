package at.xirado.simplejson;

import org.jetbrains.annotations.NotNull;

/**
 * Allows custom serialization for JSON payloads of an object.
 */
public interface SerializableArray extends JSONProperty {
    /**
     * Serialized {@link JSONArray} for this object.
     *
     * @return {@link JSONArray}
     */
    @NotNull
    JSONArray toJSONArray();
}
