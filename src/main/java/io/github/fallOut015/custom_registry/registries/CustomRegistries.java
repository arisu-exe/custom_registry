package io.github.fallOut015.custom_registry.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.github.fallOut015.custom_registry.serialization.Serializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class CustomRegistries {
    private static final Map<ResourceLocation, Function<String, CustomRegistry<?>>> TYPE_MAP;

    static {
        TYPE_MAP = new HashMap<>();

        register(ForgeRegistries.ITEMS, t -> {
            JsonObject object = new JsonObject();
            object.addProperty("class", t.get().getClass().getSimpleName());
            return object;
        }, jsonElement -> {
            Class<?> clazz = getClass(jsonElement, "net.minecraft.world.item.", Item.class);
            Constructor<?> constructor = Objects.requireNonNull(clazz).getConstructors()[0];
            return () -> {
                try {
                    return (Item) (constructor.newInstance(getParameters(jsonElement, constructor)));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    return null;
                }
            };
        });

        register(ForgeRegistries.BLOCKS, t -> JsonNull.INSTANCE, jsonElement -> {
            Class<?> clazz = getClass(jsonElement, "net.minecraft.world.level.block.", Block.class);
            Constructor<?> constructor = Objects.requireNonNull(clazz).getConstructors()[0];
            return () -> {
                try {
                    return (Block) (constructor.newInstance(getParameters(jsonElement, constructor)));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    return null;
                }
            };
        });

        register(ForgeRegistries.SOUND_EVENTS, t -> JsonNull.INSTANCE, jsonElement -> () -> new SoundEvent(new ResourceLocation("x")));

        register(ForgeRegistries.FLUIDS, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.MOB_EFFECTS, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.POTIONS, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.ATTRIBUTES, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.STAT_TYPES, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.ENCHANTMENTS, t -> JsonNull.INSTANCE, jsonElement -> {
            Class<?> clazz = getClass(jsonElement, "net.minecraft.world.item.enchantment.", Enchantment.class);
            Constructor<?> constructor = Objects.requireNonNull(clazz).getConstructors()[0];
            return () -> {
                try {
                    return (Enchantment) (constructor.newInstance(getParameters(jsonElement, constructor)));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    return null;
                }
            };
        });

        register(ForgeRegistries.ENTITIES, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.PAINTING_TYPES, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.PARTICLE_TYPES, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.CONTAINERS, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.BLOCK_ENTITIES, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.RECIPE_SERIALIZERS, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.PROFESSIONS, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.POI_TYPES, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.MEMORY_MODULE_TYPES, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.SENSOR_TYPES, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.SCHEDULES, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.ACTIVITIES, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.CHUNK_STATUS, t -> JsonNull.INSTANCE, jsonElement -> () -> null);

        register(ForgeRegistries.DATA_SERIALIZERS, t -> JsonNull.INSTANCE, jsonElement -> () -> null);
    }

    private static <T extends IForgeRegistryEntry<T>> void register(IForgeRegistry<T> forgeRegistry, Function<Supplier<T>, JsonElement> jsonBuilder, Function<JsonElement, Supplier<T>> jsonParser) {
        TYPE_MAP.put(forgeRegistry.getRegistryName(), modid -> new CustomRegistry<>(modid, forgeRegistry, jsonBuilder, jsonParser));
    }

    public static <T extends IForgeRegistryEntry<T>> CustomRegistry<T> constructGet(ResourceLocation registry, String namespace) {
        return (CustomRegistry<T>) TYPE_MAP.get(registry).apply(namespace);
    }

    private static @Nullable Class<?> getClass(JsonElement jsonElement, String path, Class<?> defaultClass) {
        @Nullable Class<?> clazz = null;
        try {
            clazz = jsonElement.getAsJsonObject().has("class") ? Class.forName(path + jsonElement.getAsJsonObject().get("class").getAsString()) : defaultClass;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }
    private static Object[] getParameters(JsonElement jsonElement, Constructor<?> constructor) {
        Object[] parameters = jsonElement.getAsJsonObject().get("parameters").isJsonArray() ? new Object[jsonElement.getAsJsonObject().get("parameters").getAsJsonArray().size()] : new Object[1];
        if(jsonElement.getAsJsonObject().get("parameters").isJsonArray()) {
            for(int i = 0; i < parameters.length; ++ i) {
                Class<?> parameterType = constructor.getParameterTypes()[i];
                parameters[i] = getValueOfJsonElement(jsonElement.getAsJsonObject().get("parameters").getAsJsonArray().get(i), parameterType);
            }
        } else {
            Class<?> parameterType = constructor.getParameterTypes()[0];
            parameters[0] = getValueOfJsonElement(jsonElement.getAsJsonObject().get("parameters"), parameterType);
        }
        return parameters;
    }
    private static Object getValueOfJsonElement(JsonElement jsonElement, Class<?> clazz) {
        if(clazz.equals(int.class)) {
            return jsonElement.getAsInt();
        } else if(clazz.equals(float.class)) {
            return jsonElement.getAsFloat();
        } else if(clazz.equals(boolean.class)) {
            return jsonElement.getAsBoolean();
        } else if(clazz.equals(byte.class)) {
            return jsonElement.getAsByte();
        } else if(clazz.equals(char.class)) {
            return jsonElement.getAsCharacter();
        } else if(clazz.equals(double.class)) {
            return jsonElement.getAsDouble();
        } else if(clazz.equals(long.class)) {
            return jsonElement.getAsLong();
        } else if(clazz.equals(short.class)) {
            return jsonElement.getAsShort();
        } else {
            if(clazz.isArray()) {
                Object[] array = new Object[jsonElement.getAsJsonArray().size()];
                for(int i = 0; i < array.length; ++ i) {
                    array[i] = getValueOfJsonElement(jsonElement.getAsJsonArray().get(i), clazz.componentType());
                }
                return array;
            } else {
                return Serializers.deserialize(clazz, jsonElement);
            }
        }
    }
}