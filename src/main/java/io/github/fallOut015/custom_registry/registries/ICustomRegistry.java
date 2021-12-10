package io.github.fallOut015.custom_registry.registries;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import io.github.fallOut015.custom_registry.world.item.ItemsCustomRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ICustomRegistry<T extends IForgeRegistryEntry<T>> {
    Map<ResourceLocation, Function<String, ICustomRegistry<?>>> TYPE_MAP = ImmutableMap.of(new ResourceLocation("item"), ItemsCustomRegistry::new);

    String getModid();
    IForgeRegistry<T> getType();
    void register(IEventBus bus);
    void registerJson(final String name, final @Nullable JsonElement jsonElement);
    Supplier<T> parseJSON(JsonElement jsonElement);

    static ICustomRegistry<?> constructGet(ResourceLocation registry, String namespace) {
        return TYPE_MAP.get(registry).apply(namespace);
    }
}