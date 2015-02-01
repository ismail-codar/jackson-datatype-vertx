package de.crunc.jackson.datatype.vertx.generator;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.base.GeneratorBase;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Generates a tree of {@link JsonElement}.
 *
 * @author Hauke Jaeger, hauke.jaeger@googlemail.com
 * @since 2.1
 */
public class JsonElementGenerator extends GeneratorBase {

    /**
     * All the states a {@link JsonElementGenerator} can have.
     */
    private enum State {

        /**
         * Indicates that the tree of the generator is empty. This is the state of a newly created generator.
         */
        Empty,

        /**
         * Indicates that the generator is currently in the object state. In this state the generator can
         * write a field name, write a field, end the object.
         */
        Object,

        /**
         * Indicates that the generator is currently in the array state. In this state the generator can
         * write a value, start a new object, start a new array, end the array.
         */
        Array,

        /**
         * Indicates that the generator is currently in the field state. In this state the generator can
         * write a value, start a new object, start a new array.
         */
        Field
    }

    /**
     * The current state of the generator. Indicates the type of the object which is on top of the element stack. Used
     * to avoid {@code instanceof} {@link JsonObject} and  {@code instanceof} {@link JsonArray} checks.
     */
    private State state = State.Empty;

    /**
     * The root element of the tree which is being generated.
     */
    private JsonElement rootElement = null;

    /**
     * Represents the current path into the tree which is being generated.
     */
    private final Deque<JsonElement> elementStack = new LinkedList<JsonElement>();

    /**
     * Represents the type of the elements of the {@link #elementStack} with an additional {@link State#Empty} at it's
     * bottom.
     */
    private final Deque<State> stateStack = new LinkedList<State>();

    /**
     * The name of the field which is being added. Must only be accessed from within state {@link State#Field}.
     */
    private String fieldName = null;

    /**
     * Creates a new generator with the given features that uses the given object codec.
     *
     * @param features The generation features that should be enabled.
     * @param codec    The codec for encoding objects.
     * @since 2.1
     */
    public JsonElementGenerator(int features, ObjectCodec codec) {
        super(features, codec);
        stateStack.push(State.Empty);
    }

    /**
     * Pushes the given object on top of the element stack and transfers the generator in state {@link State#Object}.
     */
    private JsonObject push(JsonObject object) {
        if (rootElement == null) {
            rootElement = object;
        }

        fieldName = null;
        state = State.Object;
        stateStack.push(State.Object);
        elementStack.push(object);
        return object;
    }

    /**
     * Pushes the given object on top of the element stack and transfers the generator in state {@link State#Array}.
     */
    private JsonArray push(JsonArray array) {
        if (rootElement == null) {
            rootElement = array;
        }

        fieldName = null;
        state = State.Array;
        stateStack.push(State.Array);
        elementStack.push(array);
        return array;
    }

    /**
     * Pushes the top element from the element stack and transfers the generator in the state corresponding to the new
     * top element.
     */
    private JsonElement pop() {
        fieldName = null;
        stateStack.pop();
        state = stateStack.peek();
        return elementStack.pop();
    }

    /**
     * Peeks the top element from the element stack. Does not make any changes to the generator's state.
     */
    @SuppressWarnings("unchecked")
    private <T extends JsonElement> T peek() {
        return (T) elementStack.peek();
    }

    /**
     * Retrieves the JSON tree that has been generated by this generator.
     *
     * @param <T> The type of the root element of the tree.
     * @return The root element of the tree. Can be {@code null} if no elements have been generated at all.
     * @since 2.1
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonElement> T get() {
        return (T) rootElement;
    }

    @Override
    public void writeStartArray() throws IOException {
        switch (state) {
            case Empty:
                push(new JsonArray());
                break;

            case Object:
                throw err("can not write start array as object property unless a field name has been set");

            case Array:
                JsonArray array = peek();
                array.add(push(new JsonArray()));
                break;

            case Field:
                JsonObject object = peek();
                object.putArray(fieldName, push(new JsonArray()));
                fieldName = null;
                break;

            default:
                throw err("can not write start array, unknown state <{0}>", state);
        }
    }

    @Override
    public void writeEndArray() throws IOException {
        switch (state) {
            case Array:
                pop();
                break;

            default:
                throw err("can not write end array in state <{0}>", state);
        }
    }

    @Override
    public void writeStartObject() throws IOException {
        switch (state) {
            case Empty:
                push(new JsonObject());
                break;

            case Object:
                throw err("can not write start object as object property unless a field name has been set");

            case Array:
                JsonArray array = peek();
                array.add(push(new JsonObject()));
                break;

            case Field:
                JsonObject object = peek();
                object.putObject(fieldName, push(new JsonObject()));
                fieldName = null;
                break;

            default:
                throw err("can not write start object, unknown state <{0}>", state);
        }
    }

    @Override
    public void writeEndObject() throws IOException {
        switch (state) {
            case Object:
                pop();
                break;

            default:
                throw err("can not write end object in state <{0}>", state);
        }
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }

        switch (state) {
            case Object:
                fieldName = name;
                state = State.Field;
                break;

            default:
                throw err("can not write field name in state <{0}>", state);
        }
    }

    @Override
    public void writeString(String text) throws IOException {
        switch (state) {
            case Array:
                JsonArray array = peek();
                array.add(text);
                break;

            case Field:
                JsonObject object = peek();
                object.putString(fieldName, text);
                fieldName = null;
                state = State.Object;
                break;

            default:
                throw err("can not write <String> in state <{0}>", state);
        }
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException {
        writeString(new String(text, offset, len));
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {
        throw new UnsupportedOperationException("writeRawUTF8String(byte[], int, int)");
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        throw new UnsupportedOperationException("writeUTF8String(byte[], int, int)");
    }

    @Override
    public void writeRaw(String text) throws IOException {
        throw new UnsupportedOperationException("writeRaw(String)");
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException {
        throw new UnsupportedOperationException("writeRaw(String, int, int)");
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException {
        throw new UnsupportedOperationException("writeRaw(char[], int, int)");
    }

    @Override
    public void writeRaw(char c) throws IOException {
        throw new UnsupportedOperationException("writeRaw(char)");
    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {
        if (offset == 0 && len == data.length) {
            writeString(b64variant.encode(data));
        } else {
            byte[] range = Arrays.copyOfRange(data, offset, offset + len);
            writeString(b64variant.encode(range));
        }
    }

    private void doWriteNumber(Number number) throws IOException {
        switch (state) {
            case Array:
                JsonArray array = peek();
                array.addNumber(number);
                break;

            case Field:
                JsonObject object = peek();
                object.putNumber(fieldName, number);
                fieldName = null;
                state = State.Object;
                break;

            default:
                throw err("can not write number in state <{0}>", state);
        }
    }

    @Override
    public void writeNumber(int number) throws IOException {
        doWriteNumber(number);
    }

    @Override
    public void writeNumber(long number) throws IOException {
        doWriteNumber(number);
    }

    @Override
    public void writeNumber(BigInteger number) throws IOException {
        doWriteNumber(number);
    }

    @Override
    public void writeNumber(double number) throws IOException {
        doWriteNumber(number);
    }

    @Override
    public void writeNumber(float number) throws IOException {
        doWriteNumber(number);
    }

    @Override
    public void writeNumber(BigDecimal number) throws IOException {
        doWriteNumber(number);
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        switch (state) {
            case Array:
                JsonArray array = peek();
                array.addBoolean(b);
                break;

            case Field:
                JsonObject object = peek();
                object.putBoolean(fieldName, b);
                fieldName = null;
                state = State.Object;
                break;

            default:
                throw err("can not write boolean in state <{0}>", state);
        }
    }

    @Override
    public void writeNull() throws IOException {
        switch (state) {
            case Array:
                JsonArray array = peek();
                array.add(null);
                break;

            case Field:
                JsonObject object = peek();
                object.putValue(fieldName, null);
                fieldName = null;
                state = State.Object;
                break;

            default:
                throw err("can not write null in state <{0}>", state);
        }
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    protected void _releaseBuffers() {
    }

    @Override
    protected void _verifyValueWrite(String typeMsg) throws IOException {
        switch (state) {
            case Array:
            case Field:
                // allowed
                break;
            default:
                throw err(typeMsg);
        }
    }

    /**
     * Shortcut for creating a new {@link JsonGenerationException} with a {@link MessageFormat} message.
     */
    private JsonGenerationException err(String message, Object... args) {

        String msg = message;

        if (args != null) {
            try {
                msg = MessageFormat.format(message, args);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return new JsonGenerationException(msg);
    }
}
