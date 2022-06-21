package at.xirado.simplejson;

import org.jetbrains.annotations.NotNull;

/**
 * Allows custom serialization for JSON payloads of an object.
 */
public interface SerializableArray {
    /**
     * Serialized {@link JSONArray} for this object.
     *
     * @return {@link JSONArray}
     */
    @NotNull
    JSONArray toJSONArray();
}
