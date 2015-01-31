package de.crunc.jackson.datatype.vertx;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Fluent builder for {@link JsonObject}.
 *
 * @author Hauke Jaeger, hauke.jaeger@googlemail.com
 * @since 2.1
 */
public class JsonObjectBuilder {

    private final Map<String, Object> values;

    private JsonObjectBuilder() {
        values = new HashMap<String, Object>();
    }

    /**
     * Adds an object field to the object.
     *
     * @param name   The name of the field.
     * @param object The object that will be added. Can be {@code null} in which case {@code null} is added.
     * @return {@code this}
     * @since 2.1
     */
    public JsonObjectBuilder put(String name, @Nullable JsonObject object) {
        values.put(name, object);
        return this;
    }

    /**
     * Adds an object field to the object.
     *
     * @param name    The name of the field.
     * @param builder The builder for the object that will be added. Can be {@code null} in which case {@code null} is
     *                added.
     * @return {@code this}
     * @since 2.1
     */
    public JsonObjectBuilder put(String name, @Nullable JsonObjectBuilder builder) {
        if (builder != null) {
            return put(name, builder.build());
        }
        return put(name, (JsonObject) null);
    }

    /**
     * Adds an array field to the object.
     *
     * @param name  The name of the field.
     * @param array The array that will be added. Can be {@code null} in which case {@code null} is added.
     * @return {@code this}
     * @since 2.1
     */
    public JsonObjectBuilder put(String name, @Nullable JsonArray array) {
        values.put(name, array);
        return this;
    }

    /**
     * Adds an array field to the object.
     *
     * @param name    The name of the field.
     * @param builder The builder for the array that will be added. Can be {@code null} in which case {@code null} is
     *                added.
     * @return {@code this}
     * @since 2.1
     */
    public JsonObjectBuilder put(String name, @Nullable JsonArrayBuilder builder) {
        if (builder != null) {
            return put(name, builder.build());
        }
        return put(name, (JsonArray) null);
    }

    /**
     * Adds a string field to the object.
     *
     * @param name   The name of the field.
     * @param string The string that will be added. Can be {@code null} in which case {@code null} is added.
     * @return {@code this}
     * @since 2.1
     */
    public JsonObjectBuilder put(String name, @Nullable String string) {
        values.put(name, string);
        return this;
    }

    /**
     * Adds a number field to the object.
     *
     * @param name   The name of the field.
     * @param number The number that will be added. Can be {@code null} in which case {@code null} is added.
     * @return {@code this}
     * @since 2.1
     */
    public JsonObjectBuilder put(String name, @Nullable Number number) {
        values.put(name, number);
        return this;
    }

    /**
     * Adds a boolean field to the object.
     *
     * @param name The name of the field.
     * @param bool The boolean value that will be added. Can be {@code null} in which case {@code null} is added.
     * @return {@code this}
     * @since 2.1
     */
    public JsonObjectBuilder put(String name, @Nullable Boolean bool) {
        values.put(name, bool);
        return this;
    }

    /**
     * Adds binary data field to the object.
     *
     * @param name  The name of the field.
     * @param bytes The binary data that will be added. Can be {@code null} in which case {@code null} is added.
     * @return {@code this}
     * @since 2.1
     */
    public JsonObjectBuilder put(String name, @Nullable byte[] bytes) {
        values.put(name, bytes);
        return this;
    }

    /**
     * Adds {@code null} field to the object.
     *
     * @param name The name of the field.
     * @return {@code this}
     * @since 2.1
     */
    public JsonObjectBuilder putNull(String name) {
        values.put(name, null);
        return this;
    }

    /**
     * Builds a new object which contains the fields that have been added to this builder so far.
     *
     * @return a new object.
     * @since 2.1
     */
    public JsonObject build() {
        JsonObject object = new JsonObject();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            object.putValue(entry.getKey(), entry.getValue());
        }

        return object;
    }

    /**
     * Builds a new object which contains the fields that have been added to this builder so far and encodes it as a
     * JSON string like {@code "{"foo":17,"bar":false}"}
     *
     * @return a JSON object string.
     * @since 2.1
     */
    public String encode() {
        return build().encode();
    }

    /**
     * Factory method for creating a new builder.
     *
     * @return A new builder.
     * @since 2.1
     */
    public static JsonObjectBuilder object() {
        return new JsonObjectBuilder();
    }
}
