package io.github.fallOut015.custom_registry.serialization;

import com.google.gson.JsonElement;

import java.util.function.Function;

public record SerializerType<T>(Function<T, JsonElement> serializer, Function<JsonElement, T> deserializer) {
    public JsonElement serialize(Object t) {
        return this.serializer.apply((T) t);
    }
    public T deserialize(JsonElement jsonElement) {
        return this.deserializer.apply(jsonElement);
    }
}