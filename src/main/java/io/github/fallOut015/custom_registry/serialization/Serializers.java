package io.github.fallOut015.custom_registry.serialization;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Serializers {
    static final Map<Class<?>, Serializer<?>> CLASS_MAP;

    static {
        CLASS_MAP = new HashMap<>();

        CLASS_MAP.put(MobEffectInstance.class, new Serializer<>(jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            MobEffect mobEffect = Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(jsonObject.get("mob_effect").getAsString())));
            int duration = jsonObject.has("duration") ? jsonObject.get("duration").getAsInt() : 0;
            int amplifier = jsonObject.has("amplifier") ? jsonObject.get("amplifier").getAsInt() : 0;
            boolean ambient = jsonObject.has("ambient") && jsonObject.get("ambient").getAsBoolean();
            boolean visible = !jsonObject.has("visible") || jsonObject.get("visible").getAsBoolean();
            boolean showIcon = jsonObject.has("show_icon") ? jsonObject.get("show_icon").getAsBoolean() : visible;
            @Nullable MobEffectInstance hiddenEffect = jsonObject.has("hidden_effect") ? (MobEffectInstance) Serializers.getSerializer(MobEffectInstance.class).deserialize(jsonObject.get("hidden_effect").getAsJsonObject()) : null;
            return new MobEffectInstance(mobEffect, duration, amplifier, ambient, visible, showIcon, hiddenEffect);
        }));

        CLASS_MAP.put(Block.class, new Serializer<>(jsonElement -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(jsonElement.getAsString()))));

        CLASS_MAP.put(Item.class, new Serializer<>(jsonElement -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(jsonElement.getAsString()))));

        CLASS_MAP.put(CreativeModeTab.class, new Serializer<>(jsonElement -> {
            return Arrays.stream(CreativeModeTab.TABS).filter(creativeModeTab -> {
                try {
                    Field langId = CreativeModeTab.class.getDeclaredField("langId");
                    boolean notAccessible = !langId.canAccess(creativeModeTab);
                    if (notAccessible) {
                        langId.setAccessible(true);
                    }
                    String ret = (String) langId.get(creativeModeTab);
                    if (notAccessible) {
                        langId.setAccessible(true);
                    }
                    if (ret.equals(jsonElement.getAsString())) {
                        return true;
                    }
                } catch (NoSuchFieldException | IllegalAccessException exception) {
                    exception.printStackTrace();
                }
                return false;
            }).collect(Collectors.toList()).get(0);
        }));

        CLASS_MAP.put(FoodProperties.class, new Serializer<>(jsonElement -> {
            FoodProperties.Builder foodProperties$Builder = new FoodProperties.Builder();
            JsonObject food = jsonElement.getAsJsonObject();
            if(food.has("nutrition")) {
                foodProperties$Builder.nutrition(food.get("nutrition").getAsInt());
            }
            if(food.has("saturation_mod")) {
                foodProperties$Builder.saturationMod(food.get("saturation_mod").getAsFloat());
            }
            if(food.has("meat") && food.get("meat").getAsBoolean()) {
                foodProperties$Builder.meat();
            }
            if(food.has("always_eat") && food.get("always_eat").getAsBoolean()) {
                foodProperties$Builder.alwaysEat();
            }
            if(food.has("fast") && food.get("fast").getAsBoolean()) {
                foodProperties$Builder.fast();
            }
            if(food.has("effect")) {
                JsonObject effect = food.get("effect").getAsJsonObject();
                foodProperties$Builder.effect(() -> (MobEffectInstance) Serializers.getSerializer(MobEffectInstance.class).deserialize(effect.get("effect_in").getAsJsonObject()), effect.get("probability").getAsFloat());
            }
            return foodProperties$Builder.build();
        }));

        CLASS_MAP.put(Rarity.class, new Serializer<>(jsonElement -> Rarity.valueOf(jsonElement.getAsString())));

        CLASS_MAP.put(Item.Properties.class, new Serializer<>(jsonElement -> {
            JsonObject properties = jsonElement.getAsJsonObject();
            Item.Properties item$properties = new Item.Properties();
            if(properties.has("food")) {
                item$properties.food((FoodProperties) CLASS_MAP.get(FoodProperties.class).deserialize(properties.get("food")));
            }
            if(properties.has("stacks_to")) {
                item$properties.stacksTo(properties.get("stacks_to").getAsInt());
            }
            if(properties.has("default_durability")) {
                item$properties.defaultDurability(properties.get("default_durability").getAsInt());
            }
            if(properties.has("durability")) {
                item$properties.durability(properties.get("durability").getAsInt());
            }
            if(properties.has("craft_remainder")) {
                item$properties.craftRemainder((Item) CLASS_MAP.get(Item.class).deserialize(properties.get("craft_remainder")));
            }
            if(properties.has("tab")) {
                item$properties.tab((CreativeModeTab) CLASS_MAP.get(CreativeModeTab.class).deserialize(properties.get("tab")));
            }
            if(properties.has("rarity")) {
                item$properties.rarity((Rarity) CLASS_MAP.get(Rarity.class).deserialize(properties.get("rarity")));
            }
            if(properties.has("fire_resistant") && properties.get("fire_resistant").getAsBoolean()) {
                item$properties.fireResistant();
            }
            if(properties.has("set_no_repair") && properties.get("set_no_repair").getAsBoolean()) {
                item$properties.setNoRepair();
            }
            return item$properties;
        }));

        CLASS_MAP.put(Tier.class, new Serializer<>(jsonElement -> Tiers.valueOf(jsonElement.getAsString())));
    }

    public static Serializer<?> getSerializer(Class<?> clazz) {
        return CLASS_MAP.get(clazz);
    }
}