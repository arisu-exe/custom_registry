package io.github.fallOut015.custom_registry.registries;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.fallOut015.custom_registry.serialization.Serializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

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

        register(ForgeRegistries.ITEMS, jsonElement -> {
            JsonObject object = jsonElement.getAsJsonObject();

            Class<?> c = null;
            try {
                c = object.has("class") ? Class.forName("net.minecraft.world.item." + object.get("class").getAsString()) : Item.class;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            Class<?> finalC = c;

            if(object.get("parameters").isJsonArray()) {
                int size = object.get("parameters").getAsJsonArray().size();
                Object[] parameters = new Object[size];
                for(int i = 0; i < size; ++ i) {
                    Class<?> parameterType = Objects.requireNonNull(finalC).getConstructors()[0].getParameterTypes()[i];
                    if(parameterType.equals(int.class)) {
                        parameters[i] = object.get("parameters").getAsJsonArray().get(i).getAsInt();
                    } else if(parameterType.equals(float.class)) {
                        parameters[i] = object.get("parameters").getAsJsonArray().get(i).getAsFloat();
                    } else if(parameterType.equals(boolean.class)) {
                        parameters[i] = object.get("parameters").getAsJsonArray().get(i).getAsBoolean();
                    } else if(parameterType.equals(byte.class)) {
                        parameters[i] = object.get("parameters").getAsJsonArray().get(i).getAsByte();
                    } else if(parameterType.equals(char.class)) {
                        parameters[i] = object.get("parameters").getAsJsonArray().get(i).getAsCharacter();
                    } else if(parameterType.equals(double.class)) {
                        parameters[i] = object.get("parameters").getAsJsonArray().get(i).getAsDouble();
                    } else if(parameterType.equals(long.class)) {
                        parameters[i] = object.get("parameters").getAsJsonArray().get(i).getAsLong();
                    } else if(parameterType.equals(short.class)) {
                        parameters[i] = object.get("parameters").getAsJsonArray().get(i).getAsShort();
                    } else {
                        parameters[i] = Serializers.deserialize(parameterType, object.get("parameters").getAsJsonArray().get(i));
                    }
                }

                return () -> {
                    try {
                        return (Item) Objects.requireNonNull(finalC).getConstructors()[0].newInstance(parameters);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return null;
                };
            } else {
                Object parameter;
                Class<?> parameterType = Objects.requireNonNull(finalC).getConstructors()[0].getParameterTypes()[0];
                if(parameterType.equals(int.class)) {
                    parameter = object.get("parameters").getAsInt();
                } else if(parameterType.equals(float.class)) {
                    parameter = object.get("parameters").getAsFloat();
                } else if(parameterType.equals(boolean.class)) {
                    parameter = object.get("parameters").getAsBoolean();
                } else if(parameterType.equals(byte.class)) {
                    parameter = object.get("parameters").getAsByte();
                } else if(parameterType.equals(char.class)) {
                    parameter = object.get("parameters").getAsCharacter();
                } else if(parameterType.equals(double.class)) {
                    parameter = object.get("parameters").getAsDouble();
                } else if(parameterType.equals(long.class)) {
                    parameter = object.get("parameters").getAsLong();
                } else if(parameterType.equals(short.class)) {
                    parameter = object.get("parameters").getAsShort();
                } else {
                    parameter = Serializers.deserialize(parameterType, object.get("parameters").getAsJsonObject());
                }

                Object finalParameter = parameter;
                return () -> {
                    try {
                        return (Item) Objects.requireNonNull(finalC).getConstructors()[0].newInstance(finalParameter);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return null;
                };
            }
        });

        register(ForgeRegistries.BLOCKS, jsonElement -> () -> new Block(BlockBehaviour.Properties.of(Material.WOOD)));

        register(ForgeRegistries.SOUND_EVENTS, jsonElement -> () -> new SoundEvent(new ResourceLocation("x")));

//      fluid

//      mob_effect

//      potion

//      attribute

//      stat_type

//      enchantment

//      entity_type

//      motive

//      particle_type

//      menu

//      block_entity_type

//      recipe_serializer

//      villager_profession

//      point_of_interest_type

//      memory_module_type

//      sensor_type

//      schedule

//      activity

//      chunk_status

//      data_serializers
    }

    private static <T extends IForgeRegistryEntry<T>> void register(IForgeRegistry<T> forgeRegistry, Function<JsonElement, Supplier<T>> jsonParser) {
        TYPE_MAP.put(forgeRegistry.getRegistryName(), modid -> new CustomRegistry<>(modid, forgeRegistry, jsonParser));
    }

    public static CustomRegistry<?> constructGet(ResourceLocation registry, String namespace) {
        return TYPE_MAP.get(registry).apply(namespace);
    }
}