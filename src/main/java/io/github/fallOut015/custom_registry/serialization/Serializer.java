package io.github.fallOut015.custom_registry.serialization;

import com.google.gson.JsonElement;

import java.util.function.Function;

public record Serializer<T>(Function<JsonElement, T> deserializer) {
    public T deserialize(JsonElement jsonElement) {
        return this.deserializer.apply(jsonElement);
    }
}