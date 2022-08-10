package at.xirado.simplejson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a list of values used in communication with the Discord API.
 *
 * <p>Throws {@link java.lang.IndexOutOfBoundsException}
 * if provided with index out of bounds.
 *
 * <p>This class is Thread-Safe
 */
public class JSONArray implements Iterable<Object>, SerializableArray {
    private static final Logger log = LoggerFactory.getLogger(JSONObject.class);
    private static final ObjectMapper mapper;
    private static final SimpleModule module;
    private static final CollectionType listType;

    static {
        mapper = new ObjectMapper();
        module = new SimpleModule();
        module.addAbstractTypeMapping(Map.class, HashMap.class);
        module.addAbstractTypeMapping(List.class, ArrayList.class);
        mapper.registerModule(module);
        listType = mapper.getTypeFactory().constructRawCollectionType(ArrayList.class);
    }

    protected final List<Object> data;

    protected JSONArray(List<Object> data) {
        this.data = Collections.synchronizedList(data);
    }

    protected JSONArray(String json) {
        try {
            this.data = mapper.readValue(json, listType);
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    protected JSONArray(InputStream stream) {
        try {
            this.data = mapper.readValue(stream, listType);
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    /**
     * Creates a new empty JSONArray, ready to be populated with values.
     *
     * @return An empty JSONArray instance
     * @see #add(Object)
     */
    @NotNull
    public static JSONArray empty() {
        return new JSONArray(new ArrayList<>());
    }

    /**
     * Creates a new JSONArray and populates it with the contents
     * of the provided collection.
     *
     * @param col The {@link java.util.Collection}
     * @return A new JSONArray populated with the contents of the collection
     */
    @NotNull
    public static JSONArray fromCollection(@NotNull Collection<?> col) {
        return empty().addAll(col);
    }

    /**
     * Parses a JSON Array into a JSONArray instance.
     *
     * @param json The correctly formatted JSON Array
     * @return A new JSONArray instance for the provided array
     * @throws ParsingException If the provided JSON is incorrectly formatted
     */
    @NotNull
    public static JSONArray fromJson(@NotNull String json) {
        return new JSONArray(json);
    }

    /**
     * Parses a JSON Array into a JSONArray instance.
     *
     * @param json The correctly formatted JSON Array
     * @return A new JSONArray instance for the provided array
     * @throws ParsingException If the provided JSON is incorrectly formatted or an I/O error occurred
     */
    @NotNull
    public static JSONArray fromJson(@NotNull InputStream json) {
        return new JSONArray(json);
    }

    /**
     * Parses a JSON Array into a JSONArray instance.
     *
     * @param json The correctly formatted JSON Array
     * @return A new JSONArray instance for the provided array
     * @throws ParsingException If the provided JSON is incorrectly formatted or an I/O error occurred
     */
    @NotNull
    public static JSONArray fromJson(@NotNull Reader json) {
        try {
            return new JSONArray((List<Object>) mapper.readValue(json, listType));
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    /**
     * Whether the value at the specified index is null.
     *
     * @param index The index to check
     * @return True, if the value at the index is null
     */
    public boolean isNull(int index) {
        return data.get(index) == null;
    }

    /**
     * Whether the value at the specified index is of the specified type.
     *
     * @param index The index to check
     * @param type  The type to check
     * @return True, if the type check is successful
     * @see DataType#isType(Object)
     */
    public boolean isType(int index, @NotNull DataType type) {
        return type.isType(data.get(index));
    }

    /**
     * The length of the array.
     *
     * @return The length of the array
     */
    public int length() {
        return data.size();
    }

    /**
     * Whether this array is empty
     *
     * @return True, if this array is empty
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Resolves the value at the specified index to a JSONObject
     *
     * @param index The index to resolve
     * @return The resolved JSONObject
     * @throws ParsingException If the value is of the wrong type or missing
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public JSONObject getObject(int index) {
        Map<String, Object> child = null;
        try {
            child = (Map<String, Object>) get(Map.class, index);
        } catch (ClassCastException ex) {
            log.error("Unable to extract child data", ex);
        }
        if (child == null)
            throw valueError(index, "JSONObject");
        return new JSONObject(child);
    }

    /**
     * Resolves the value at the specified index to a JSONArray
     *
     * @param index The index to resolve
     * @return The resolved JSONArray
     * @throws ParsingException If the value is of the wrong type or null
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public JSONArray getArray(int index) {
        List<Object> child = null;
        try {
            child = (List<Object>) get(List.class, index);
        } catch (ClassCastException ex) {
            log.error("Unable to extract child data", ex);
        }
        if (child == null)
            throw valueError(index, "JSONArray");
        return new JSONArray(child);
    }

    /**
     * Resolves the value at the specified index to a String.
     *
     * @param index The index to resolve
     * @return The resolved String
     * @throws ParsingException If the value is of the wrong type or null
     */
    @NotNull
    public String getString(int index) {
        String value = get(String.class, index, UnaryOperator.identity(), String::valueOf);
        if (value == null)
            throw valueError(index, "String");
        return value;
    }

    /**
     * Resolves the value at the specified index to a String.
     *
     * @param index        The index to resolve
     * @param defaultValue Alternative value to use when the value associated with the index is null
     * @return The resolved String
     * @throws ParsingException If the value is of the wrong type
     */
    @Contract("_, !null -> !null")
    public String getString(int index, @Nullable String defaultValue) {
        String value = get(String.class, index, UnaryOperator.identity(), String::valueOf);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to a boolean.
     *
     * @param index The index to resolve
     * @return True, if the value is present and set to true. Otherwise false.
     * @throws ParsingException If the value is of the wrong type
     */
    public boolean getBoolean(int index) {
        return getBoolean(index, false);
    }

    /**
     * Resolves the value at the specified index to a boolean.
     *
     * @param index        The index to resolve
     * @param defaultValue Alternative value to use when the value associated with the index is null
     * @return True, if the value is present and set to true. False, if it is set to false. Otherwise defaultValue.
     * @throws ParsingException If the value is of the wrong type
     */
    public boolean getBoolean(int index, boolean defaultValue) {
        Boolean value = get(Boolean.class, index, Boolean::parseBoolean, null);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to an int.
     *
     * @param index The index to resolve
     * @return The resolved int value
     * @throws ParsingException If the value is of the wrong type
     */
    public int getInt(int index) {
        Integer value = get(Integer.class, index, Integer::parseInt, Number::intValue);
        if (value == null)
            throw valueError(index, "int");
        return value;
    }

    /**
     * Resolves the value at the specified index to an int.
     *
     * @param index        The index to resolve
     * @param defaultValue Alternative value to use when the value associated with the index is null
     * @return The resolved int value
     * @throws ParsingException If the value is of the wrong type
     */
    public int getInt(int index, int defaultValue) {
        Integer value = get(Integer.class, index, Integer::parseInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to a double.
     *
     * @param index The index to resolve
     * @return The resolved double value
     * @throws ParsingException If the value is of the wrong type
     */
    public double getDouble(int index) {
        Double value = get(Double.class, index, Double::parseDouble, Number::doubleValue);
        if (value == null)
            throw valueError(index, "double");
        return value;
    }

    /**
     * Resolves the value at the specified index to a double.
     *
     * @param index        The index to resolve
     * @param defaultValue Alternative value to use when the value associated with the index is null
     * @return The resolved double value
     * @throws ParsingException If the value is of the wrong type
     */
    public double getDouble(int index, double defaultValue) {
        Double value = get(Double.class, index, Double::parseDouble, Number::doubleValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to an unsigned int.
     *
     * @param index The index to resolve
     * @return The resolved unsigned int value
     * @throws ParsingException If the value is of the wrong type
     */
    public int getUnsignedInt(int index) {
        Integer value = get(Integer.class, index, Integer::parseUnsignedInt, Number::intValue);
        if (value == null)
            throw valueError(index, "unsigned int");
        return value;
    }

    /**
     * Resolves the value at the specified index to an unsigned int.
     *
     * @param index        The index to resolve
     * @param defaultValue Alternative value to use when the value associated with the index is null
     * @return The resolved unsigned int value
     * @throws ParsingException If the value is of the wrong type
     */
    public int getUnsignedInt(int index, int defaultValue) {
        Integer value = get(Integer.class, index, Integer::parseUnsignedInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to a long.
     *
     * @param index The index to resolve
     * @return The resolved long value
     * @throws ParsingException If the value is of the wrong type
     */
    public long getLong(int index) {
        Long value = get(Long.class, index, Long::parseLong, Number::longValue);
        if (value == null)
            throw valueError(index, "long");
        return value;
    }

    /**
     * Resolves the value at the specified index to a long.
     *
     * @param index        The index to resolve
     * @param defaultValue Alternative value to use when the value associated with the index is null
     * @return The resolved long value
     * @throws ParsingException If the value is of the wrong type
     */
    public long getLong(int index, long defaultValue) {
        Long value = get(Long.class, index, Long::parseLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to an unsigned long.
     *
     * @param index The index to resolve
     * @return The resolved unsigned long value
     * @throws ParsingException If the value is of the wrong type
     */
    public long getUnsignedLong(int index) {
        Long value = get(Long.class, index, Long::parseUnsignedLong, Number::longValue);
        if (value == null)
            throw valueError(index, "unsigned long");
        return value;
    }

    /**
     * Resolves the value at the specified index to an unsigned long.
     *
     * @param index        The index to resolve
     * @param defaultValue Alternative value to use when the value associated with the index is null
     * @return The resolved unsigned long value
     * @throws ParsingException If the value is of the wrong type
     */
    public long getUnsignedLong(int index, long defaultValue) {
        Long value = get(Long.class, index, Long::parseUnsignedLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Appends the provided value to the end of the array.
     *
     * @param value The value to append
     * @return A JSONArray with the value inserted at the end
     */
    @NotNull
    public JSONArray add(@Nullable Object value) {
        if (value instanceof SerializableData)
            data.add(((SerializableData) value).toData().data);
        else if (value instanceof SerializableArray)
            data.add(((SerializableArray) value).toJSONArray().data);
        else
            data.add(value);
        return this;
    }

    /**
     * Appends the provided values to the end of the array.
     *
     * @param values The values to append
     * @return A JSONArray with the values inserted at the end
     */
    @NotNull
    public JSONArray addAll(@NotNull Collection<?> values) {
        values.forEach(this::add);
        return this;
    }

    /**
     * Appends the provided values to the end of the array.
     *
     * @param array The values to append
     * @return A JSONArray with the values inserted at the end
     */
    @NotNull
    public JSONArray addAll(@NotNull JSONArray array) {
        return addAll(array.data);
    }

    /**
     * Inserts the specified value at the provided index.
     *
     * @param index The target index
     * @param value The value to insert
     * @return A JSONArray with the value inserted at the specified index
     */
    @NotNull
    public JSONArray insert(int index, @Nullable Object value) {
        if (value instanceof SerializableData)
            data.add(index, ((SerializableData) value).toData().data);
        else if (value instanceof SerializableArray)
            data.add(index, ((SerializableArray) value).toJSONArray().data);
        else
            data.add(index, value);
        return this;
    }

    /**
     * Removes the value at the specified index.
     *
     * @param index The target index to remove
     * @return A JSONArray with the value removed
     */
    @NotNull
    public JSONArray remove(int index) {
        data.remove(index);
        return this;
    }

    /**
     * Removes the specified value.
     *
     * @param value The value to remove
     * @return A JSONArray with the value removed
     */
    @NotNull
    public JSONArray remove(@Nullable Object value) {
        data.remove(value);
        return this;
    }

    /**
     * Serializes this object as JSON.
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
     * Converts this JSONArray to a {@link java.util.List}.
     *
     * @return The resulting list
     */
    @NotNull
    public List<Object> toList() {
        return data;
    }

    private ParsingException valueError(int index, String expectedType) {
        return new ParsingException("Unable to resolve value at " + index + " to type " + expectedType + ": " + data.get(index));
    }

    @Nullable
    private <T> T get(@NotNull Class<T> type, int index) {
        return get(type, index, null, null);
    }

    @Nullable
    private <T> T get(@NotNull Class<T> type, int index, @Nullable Function<String, T> stringMapper, @Nullable Function<Number, T> numberMapper) {
        Object value = data.get(index);
        if (value == null)
            return null;
        if (type.isInstance(value))
            return type.cast(value);
        if (type == String.class)
            return type.cast(value.toString());
        // attempt type coercion
        if (stringMapper != null && value instanceof String)
            return stringMapper.apply((String) value);
        else if (numberMapper != null && value instanceof Number)
            return numberMapper.apply((Number) value);

        throw new ParsingException(String.format(Locale.ROOT, "Cannot parse value for index %d into type %s: %s instance of %s",
                index, type.getSimpleName(), value, value.getClass().getSimpleName()));
    }

    @NotNull
    @Override
    public Iterator<Object> iterator() {
        synchronized (data) {
            return data.iterator();
        }
    }

    @NotNull
    public <T> Stream<T> stream(BiFunction<? super JSONArray, Integer, ? extends T> mapper) {
        return IntStream.range(0, length())
                .mapToObj(index -> mapper.apply(this, index));
    }

    @NotNull
    @Override
    public JSONArray toJSONArray() {
        return this;
    }
}
