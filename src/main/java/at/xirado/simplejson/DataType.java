package at.xirado.simplejson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Enum constants representing possible types for a {@link JSONObject} value.
 */
public enum DataType
{
    INT, FLOAT, STRING, OBJECT, ARRAY, BOOLEAN, NULL, UNKNOWN;

    /**
     * Assumes the type of the provided value through instance checks.
     *
     * @param  value
     *         The value to test
     *
     * @return The DataType constant or {@link #UNKNOWN}
     */
    @NotNull
    public static DataType getType(@Nullable Object value)
    {
        for (DataType type : values())
        {
            if (type.isType(value))
                return type;
        }
        return UNKNOWN;
    }

    /**
     * Tests whether the type for the provided value is
     * the one represented by this enum constant.
     *
     * @param  value
     *         The value to check
     *
     * @return True, if the value is of this type
     */
    public boolean isType(@Nullable Object value)
    {
        switch (this)
        {
            case INT:
                return value instanceof Integer ||value instanceof Long || value instanceof Short || value instanceof Byte;
            case FLOAT:
                return value instanceof Double || value instanceof Float;
            case STRING:
                return value instanceof String;
            case BOOLEAN:
                return value instanceof Boolean;
            case ARRAY:
                return value instanceof List;
            case OBJECT:
                return value instanceof Map;
            case NULL:
                return value == null;
            default:
                return false;
        }
    }
}
