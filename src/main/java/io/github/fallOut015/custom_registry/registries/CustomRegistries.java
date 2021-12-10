package io.github.fallOut015.custom_registry.registries;

import com.google.gson.JsonObject;
import io.github.fallOut015.custom_registry.serialization.Serializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class CustomRegistries {
    private static final Map<ResourceLocation, Function<String, CustomRegistry<?>>> TYPE_MAP;

    static {
        TYPE_MAP = new HashMap<>();

        TYPE_MAP.put(new ResourceLocation("item"), modid -> new CustomRegistry<>(modid, ForgeRegistries.ITEMS, jsonElement -> {
            JsonObject object = jsonElement.getAsJsonObject();

            Class<?> c = null;
            try {
                c = object.has("class") ? Class.forName("net.minecraft.world.item." + object.get("class").getAsString()) : Item.class;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            Class<?> finalC = c;

            int size = object.get("parameters").getAsJsonArray().size();
            Object[] parameters = new Object[size];
            for(int i = 0; i < size; ++ i) {
                Class<?> parameterType = Objects.requireNonNull(finalC).getConstructors()[0].getParameterTypes()[i];
                parameters[i] = Serializers.getSerializer(parameterType).deserialize(object.get("parameters").getAsJsonArray().get(i));
            }

            return () -> {
                try {
                    return (Item) Objects.requireNonNull(finalC).getConstructors()[0].newInstance(parameters);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return null;
            };
        }));
        TYPE_MAP.put(new ResourceLocation("block"), modid -> new CustomRegistry<>(modid, ForgeRegistries.BLOCKS, jsonElement -> () -> new Block(BlockBehaviour.Properties.of(Material.WOOD))));
    }

    public static CustomRegistry<?> constructGet(ResourceLocation registry, String namespace) {
        return TYPE_MAP.get(registry).apply(namespace);
    }
}