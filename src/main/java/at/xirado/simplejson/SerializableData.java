package at.xirado.simplejson;

import org.jetbrains.annotations.NotNull;

public interface SerializableData extends JSONProperty {
    /**
     * Serialized {@link JSONObject} for this object.
     *
     * @return {@link JSONObject}
     */
    @NotNull
    JSONObject toData();
}
