package at.xirado.simplejson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Represents a map of values.
 *
 * <p>Throws {@link java.lang.NullPointerException},
 * if a parameter annotated with {@link NotNull} is provided with {@code null}.
 *
 * <p>This class is Thread-Safe.
 */
public class JSONObject implements SerializableData {
    private static final Logger log = LoggerFactory.getLogger(JSONObject.class);
    private static final ObjectMapper mapper;
    private static final ObjectMapper ymlMapper;
    private static final SimpleModule module;
    private static final MapType mapType;

    static {
        mapper = new ObjectMapper();
        ymlMapper = new ObjectMapper(new YAMLFactory());
        module = new SimpleModule();
        module.addAbstractTypeMapping(Map.class, ConcurrentHashMap.class);
        module.addAbstractTypeMapping(List.class, ArrayList.class);
        mapper.registerModule(module);
        mapType = mapper.getTypeFactory().constructRawMapType(HashMap.class);
    }

    protected final Map<String, Object> data;

    protected JSONObject(@NotNull Map<String, Object> data) {
        this.data = new ConcurrentHashMap<>(data);
    }

    protected JSONObject(@NotNull String data, @NotNull FileType fileType) {
        try {
            Map<String, Object> map = getMapper(fileType).readValue(data, mapType);
            this.data = new ConcurrentHashMap<>(map);
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
    }

    protected JSONObject(@NotNull InputStream stream, @NotNull FileType fileType) {
        try {
            Map<String, Object> map = getMapper(fileType).readValue(stream, mapType);
            this.data = new ConcurrentHashMap<>(map);
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
    }

    /**
     * Creates a new empty JSONObject, ready to be populated with values.
     *
     * @return An empty JSONObject instance
     * @see #put(String, Object)
     */
    @NotNull
    public static JSONObject empty() {
        return new JSONObject(new HashMap<>());
    }

    /**
     * Parses a JSON payload into a JSONObject instance.
     *
     * @param data The correctly formatted JSON payload to parse
     * @return A JSONObject instance for the provided payload
     * @throws ParsingException If the provided json is incorrectly formatted
     */
    @NotNull
    public static JSONObject fromJson(@NotNull byte[] data) {
        try {
            Map<String, Object> map = mapper.readValue(data, mapType);
            return new JSONObject(map);
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
    }

    /**
     * Parses a YAML payload into a JSONObject instance.
     *
     * @param yml The correctly formatted YAML payload to parse
     * @return A JSONObject instance for the provided payload
     * @throws ParsingException If the provided yaml is incorrectly formatted
     */
    @NotNull
    public static JSONObject fromYaml(@NotNull String yml) {
        try {
            Map<String, Object> map = ymlMapper.readValue(yml, mapType);
            return new JSONObject(map);
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
    }

    /**
     * Parses a JSON payload into a JSONObject instance.
     *
     * @param json The correctly formatted JSON payload to parse
     * @return A JSONObject instance for the provided payload
     * @throws ParsingException If the provided json is incorrectly formatted
     */
    @NotNull
    public static JSONObject fromJson(@NotNull String json) {
        return new JSONObject(json, FileType.JSON);
    }

    /**
     * Parses a JSON payload into a JSONObject instance.
     *
     * @param stream The correctly formatted JSON payload to parse
     * @return A JSONObject instance for the provided payload
     * @throws ParsingException If the provided json is incorrectly formatted or an I/O error occurred
     */
    @NotNull
    public static JSONObject fromJson(@NotNull InputStream stream) {
        return new JSONObject(stream, FileType.JSON);
    }

    /**
     * Parses a JSON payload into a JSONObject instance.
     *
     * @param stream The correctly formatted JSON payload to parse
     * @return A JSONObject instance for the provided payload
     * @throws ParsingException If the provided json is incorrectly formatted or an I/O error occurred
     */
    @NotNull
    public static JSONObject fromJson(@NotNull Reader stream) {
        try {
            Map<String, Object> map = mapper.readValue(stream, mapType);
            return new JSONObject(map);
        } catch (IOException ex) {
            throw new ParsingException(ex);
        }
    }

    /**
     * Whether the specified key is present.
     *
     * @param key The key to check
     * @return True, if the specified key is present
     */
    public boolean hasKey(@NotNull String key) {
        return data.containsKey(key);
    }

    /**
     * Whether the specified key is missing or null
     *
     * @param key The key to check
     * @return True, if the specified key is null or missing
     */
    public boolean isNull(@NotNull String key) {
        return data.get(key) == null;
    }

    /**
     * Whether the specified key is of the specified type.
     *
     * @param key  The key to check
     * @param type The type to check
     * @return True, if the type check is successful
     * @see DataType#isType(Object)
     */
    public boolean isType(@NotNull String key, @NotNull DataType type) {
        return type.isType(data.get(key));
    }

    /**
     * Resolves a JSONObject to a key.
     *
     * @param key The key to check for a value
     * @return The resolved instance of JSONObject for the key
     * @throws ParsingException If the type is incorrect or no value is present for the specified key
     */
    @NotNull
    public JSONObject getObject(@NotNull String key) {
        return optObject(key).orElseThrow(() -> valueError(key, "JSONObject"));
    }

    /**
     * Resolves a JSONObject to a key.
     *
     * @param key The key to check for a value
     * @return The resolved instance of JSONObject for the key, wrapped in {@link java.util.Optional}
     * @throws ParsingException If the type is incorrect
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public Optional<JSONObject> optObject(@NotNull String key) {
        Map<String, Object> child = null;
        try {
            child = (Map<String, Object>) get(Map.class, key);
        } catch (ClassCastException ex) {
            log.error("Unable to extract child data", ex);
        }
        return child == null ? Optional.empty() : Optional.of(new JSONObject(child));
    }

    /**
     * Resolves a JSONArray to a key.
     *
     * @param key The key to check for a value
     * @return The resolved instance of JSONArray for the key
     * @throws ParsingException If the type is incorrect or no value is present for the specified key
     */
    @NotNull
    public JSONArray getArray(@NotNull String key) {
        return optArray(key).orElseThrow(() -> valueError(key, "JSONArray"));
    }

    /**
     * Resolves a JSONArray to a key.
     *
     * @param key The key to check for a value
     * @return The resolved instance of JSONArray for the key, wrapped in {@link java.util.Optional}
     * @throws ParsingException If the type is incorrect
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public Optional<JSONArray> optArray(@NotNull String key) {
        List<Object> child = null;
        try {
            child = (List<Object>) get(List.class, key);
        } catch (ClassCastException ex) {
            log.error("Unable to extract child data", ex);
        }
        return child == null ? Optional.empty() : Optional.of(new JSONArray(child));
    }

    /**
     * Resolves any type to the provided key.
     *
     * @param key The key to check for a value
     * @return {@link java.util.Optional} with a possible value
     */
    @NotNull
    public Optional<Object> opt(@NotNull String key) {
        return Optional.ofNullable(data.get(key));
    }

    /**
     * Resolves any type to the provided key.
     *
     * @param key The key to check for a value
     * @return The value of any type
     * @throws ParsingException If the value is missing or null
     * @see #opt(String)
     */
    @NotNull
    public Object get(@NotNull String key) {
        Object value = data.get(key);
        if (value == null)
            throw valueError(key, "any");
        return value;
    }

    /**
     * Resolves a {@link java.lang.String} to a key.
     *
     * @param key The key to check for a value
     * @return The String value
     * @throws ParsingException If the value is missing or null
     */
    @NotNull
    public String getString(@NotNull String key) {
        String value = getString(key, null);
        if (value == null)
            throw valueError(key, "String");
        return value;
    }

    /**
     * Resolves a {@link java.lang.String} to a key.
     *
     * @param key          The key to check for a value
     * @param defaultValue Alternative value to use when no value or null value is associated with the key
     * @return The String value, or null if provided with null defaultValue
     */
    @Contract("_, !null -> !null")
    public String getString(@NotNull String key, @Nullable String defaultValue) {
        String value = get(String.class, key, UnaryOperator.identity(), String::valueOf);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves a {@link java.lang.Boolean} to a key.
     *
     * @param key The key to check for a value
     * @return True, if the value is present and set to true. False if the value is missing or set to false.
     * @throws ParsingException If the value is of the wrong type
     */
    public boolean getBoolean(@NotNull String key) {
        return getBoolean(key, false);
    }

    /**
     * Resolves a {@link java.lang.Boolean} to a key.
     *
     * @param key          The key to check for a value
     * @param defaultValue Alternative value to use when no value or null value is associated with the key
     * @return True, if the value is present and set to true. False if the value is set to false. defaultValue if it is missing.
     * @throws ParsingException If the value is of the wrong type
     */
    public boolean getBoolean(@NotNull String key, boolean defaultValue) {
        Boolean value = get(Boolean.class, key, Boolean::parseBoolean, null);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves a long to a key.
     *
     * @param key The key to check for a value
     * @return The long value for the key
     * @throws ParsingException If the value is missing, null, or of the wrong type
     */
    public long getLong(@NotNull String key) {
        Long value = get(Long.class, key, MiscUtil::parseLong, Number::longValue);
        if (value == null)
            throw valueError(key, "long");
        return value;
    }

    /**
     * Resolves a long to a key.
     *
     * @param key          The key to check for a value
     * @param defaultValue Alternative value to use when no value or null value is associated with the key
     * @return The long value for the key
     * @throws ParsingException If the value is of the wrong type
     */
    public long getLong(@NotNull String key, long defaultValue) {
        Long value = get(Long.class, key, Long::parseLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves an unsigned long to a key.
     *
     * @param key The key to check for a value
     * @return The unsigned long value for the key
     * @throws ParsingException If the value is missing, null, or of the wrong type
     */
    public long getUnsignedLong(@NotNull String key) {
        Long value = get(Long.class, key, Long::parseUnsignedLong, Number::longValue);
        if (value == null)
            throw valueError(key, "unsigned long");
        return value;
    }

    /**
     * Resolves an unsigned long to a key.
     *
     * @param key          The key to check for a value
     * @param defaultValue Alternative value to use when no value or null value is associated with the key
     * @return The unsigned long value for the key
     * @throws ParsingException If the value is of the wrong type
     */
    public long getUnsignedLong(@NotNull String key, long defaultValue) {
        Long value = get(Long.class, key, Long::parseUnsignedLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves an int to a key.
     *
     * @param key The key to check for a value
     * @return The int value for the key
     * @throws ParsingException If the value is missing, null, or of the wrong type
     */
    public int getInt(@NotNull String key) {
        Integer value = get(Integer.class, key, Integer::parseInt, Number::intValue);
        if (value == null)
            throw valueError(key, "int");
        return value;
    }

    /**
     * Resolves an int to a key.
     *
     * @param key          The key to check for a value
     * @param defaultValue Alternative value to use when no value or null value is associated with the key
     * @return The int value for the key
     * @throws ParsingException If the value is of the wrong type
     */
    public int getInt(@NotNull String key, int defaultValue) {
        Integer value = get(Integer.class, key, Integer::parseInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves an unsigned int to a key.
     *
     * @param key The key to check for a value
     * @return The unsigned int value for the key
     * @throws ParsingException If the value is missing, null, or of the wrong type
     */
    public int getUnsignedInt(@NotNull String key) {
        Integer value = get(Integer.class, key, Integer::parseUnsignedInt, Number::intValue);
        if (value == null)
            throw valueError(key, "unsigned int");
        return value;
    }

    /**
     * Resolves an unsigned int to a key.
     *
     * @param key          The key to check for a value
     * @param defaultValue Alternative value to use when no value or null value is associated with the key
     * @return The unsigned int value for the key
     * @throws ParsingException If the value is of the wrong type
     */
    public int getUnsignedInt(@NotNull String key, int defaultValue) {
        Integer value = get(Integer.class, key, Integer::parseUnsignedInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves a double to a key.
     *
     * @param key The key to check for a value
     * @return The double value for the key
     * @throws ParsingException If the value is missing, null, or of the wrong type
     */
    public double getDouble(@NotNull String key) {
        Double value = get(Double.class, key, Double::parseDouble, Number::doubleValue);
        if (value == null)
            throw valueError(key, "double");
        return value;
    }

    /**
     * Resolves a double to a key.
     *
     * @param key          The key to check for a value
     * @param defaultValue Alternative value to use when no value or null value is associated with the key
     * @return The double value for the key
     * @throws ParsingException If the value is of the wrong type
     */
    public double getDouble(@NotNull String key, double defaultValue) {
        Double value = get(Double.class, key, Double::parseDouble, Number::doubleValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Removes the value associated with the specified key.
     * If no value is associated with the key, this does nothing.
     *
     * @param key The key to unlink
     * @return A JSONObject with the removed key
     */
    @NotNull
    public JSONObject remove(@NotNull String key) {
        data.remove(key);
        return this;
    }

    /**
     * Upserts a null value for the provided key.
     *
     * @param key The key to upsert
     * @return A JSONObject with the updated value
     */
    @NotNull
    public JSONObject putNull(@NotNull String key) {
        data.put(key, null);
        return this;
    }

    /**
     * Upserts a new value for the provided key.
     *
     * @param key   The key to upsert
     * @param value The new value
     * @return A JSONObject with the updated value
     */
    @NotNull
    public JSONObject put(@NotNull String key, @Nullable Object value) {
        if (value instanceof SerializableData)
            data.put(key, ((SerializableData) value).toData().data);
        else if (value instanceof SerializableArray)
            data.put(key, ((SerializableArray) value).toJSONArray().data);
        else
            data.put(key, value);
        return this;
    }

    /**
     * {@link java.util.Collection} of all values in this JSONObject.
     *
     * @return {@link java.util.Collection} for all values
     */
    @NotNull
    public Collection<Object> values() {
        return data.values();
    }

    /**
     * {@link java.util.Set} of all keys in this JSONObject.
     *
     * @return {@link Set} of keys
     */
    @NotNull
    public Set<String> keys() {
        return data.keySet();
    }

    /**
     * Serialize this object as JSON.
     *
     * @return byte array containing the JSON representation of this object
     */
    @NotNull
    public byte[] toJson() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mapper.writeValue(outputStream, data);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new ParsingException(e);
        }
    }

    @NotNull
    public String toPrettyString() {
        DefaultPrettyPrinter.Indenter indent = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.withObjectIndenter(indent).withArrayIndenter(indent);
        try {
            return mapper.writer(printer).writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new ParsingException(e);
        }
    }

    /**
     * Converts this JSONObject to a {@link java.util.Map}
     *
     * @return The resulting map
     */
    @NotNull
    public Map<String, Object> toMap() {
        return data;
    }

    @NotNull
    @Override
    public JSONObject toData() {
        return this;
    }

    private ParsingException valueError(String key, String expectedType) {
        return new ParsingException("Unable to resolve value with key " + key + " to type " + expectedType + ": " + data.get(key));
    }

    @Nullable
    private <T> T get(@NotNull Class<T> type, @NotNull String key) {
        return get(type, key, null, null);
    }

    @Nullable
    private <T> T get(@NotNull Class<T> type, @NotNull String key, @Nullable Function<String, T> stringParse, @Nullable Function<Number, T> numberParse) {
        Object value = data.get(key);
        if (value == null)
            return null;
        if (type.isInstance(value))
            return type.cast(value);
        if (type == String.class)
            return type.cast(value.toString());
        // attempt type coercion
        if (value instanceof Number && numberParse != null)
            return numberParse.apply((Number) value);
        else if (value instanceof String && stringParse != null)
            return stringParse.apply((String) value);

        throw new ParsingException(String.format(Locale.ROOT, "Cannot parse value for %s into type %s: %s instance of %s",
                key, type.getSimpleName(), value, value.getClass().getSimpleName()));
    }

    private ObjectMapper getMapper(FileType fileType) {
        switch(fileType) {
            case JSON:
                return mapper;
            case YAML:
                return ymlMapper;
            default:
                throw new IllegalArgumentException("Unsupported Type");
        }
    }
}
