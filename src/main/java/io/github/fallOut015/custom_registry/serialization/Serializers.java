package io.github.fallOut015.custom_registry.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Serializers {
    /*static class Map<T> {
        static SerializerType<?> SERIALIZER_TYPE;
    }*/
    static final Map<Class<?>, SerializerType<?>> CLASS_MAP;

    static {
        CLASS_MAP = new HashMap<>();

        register(MobEffectInstance.class, t -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("mob_effect", Objects.requireNonNull(t.getEffect().getRegistryName()).toString());
            if(t.getDuration() != 0) {
                jsonObject.addProperty("duration", t.getDuration());
            }
            if(t.getAmplifier() != 0) {
                jsonObject.addProperty("amplifier", t.getAmplifier());
            }
            if(t.isAmbient()) {
                jsonObject.addProperty("ambient", true);
            }
            if(!t.isVisible()) {
                jsonObject.addProperty("visible", false);
            }
            if(!t.showIcon()) {
                jsonObject.addProperty("show_icon", false);
            }
            try {
                Field hiddenEffect = MobEffectInstance.class.getDeclaredField("hiddenEffect");
                boolean notAccessible = !hiddenEffect.canAccess(t);
                if (notAccessible) {
                    hiddenEffect.setAccessible(true);
                }
                @Nullable MobEffectInstance ret = (MobEffectInstance) hiddenEffect.get(t);
                if (notAccessible) {
                    hiddenEffect.setAccessible(true);
                }
                if(ret != null) {
                    jsonObject.add("hidden_effect", Serializers.serialize(ret));
                }
            } catch(NoSuchFieldException | IllegalAccessException exception) {
                exception.printStackTrace();
            }
            return jsonObject;
        }, jsonElement -> {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            MobEffect mobEffect = Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(jsonObject.get("mob_effect").getAsString())));
            int duration = jsonObject.has("duration") ? jsonObject.get("duration").getAsInt() : 0;
            int amplifier = jsonObject.has("amplifier") ? jsonObject.get("amplifier").getAsInt() : 0;
            boolean ambient = jsonObject.has("ambient") && jsonObject.get("ambient").getAsBoolean();
            boolean visible = !jsonObject.has("visible") || jsonObject.get("visible").getAsBoolean();
            boolean showIcon = jsonObject.has("show_icon") ? jsonObject.get("show_icon").getAsBoolean() : visible;
            @Nullable MobEffectInstance hiddenEffect = jsonObject.has("hidden_effect") ? (MobEffectInstance) Serializers.getSerializer(MobEffectInstance.class).deserialize(jsonObject.get("hidden_effect").getAsJsonObject()) : null;
            return new MobEffectInstance(mobEffect, duration, amplifier, ambient, visible, showIcon, hiddenEffect);
        });

        register(Block.class, t -> {
            return new JsonPrimitive(Objects.requireNonNull(t.getRegistryName()).toString());
        }, jsonElement -> {
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(jsonElement.getAsString()));
        }); // TODO replace with suppliers? Or functions?

        register(Item.class, t -> {
            return new JsonPrimitive(Objects.requireNonNull(t.getRegistryName()).toString());
        }, jsonElement -> {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation(jsonElement.getAsString()));
        });

        register(CreativeModeTab.class, t -> {
            try {
                Field langId = CreativeModeTab.class.getDeclaredField("langId");
                boolean notAccessible = !langId.canAccess(t);
                if (notAccessible) {
                    langId.setAccessible(true);
                }
                String ret = (String) langId.get(t);
                if (notAccessible) {
                    langId.setAccessible(true);
                }
                return new JsonPrimitive(ret);
            } catch(NoSuchFieldException | IllegalAccessException exception) {
                exception.printStackTrace();
            }
            return new JsonPrimitive("");
        }, jsonElement -> {
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
        });

        register(FoodProperties.class, t -> {
            JsonObject jsonObject = new JsonObject();
            if(t.getNutrition() != 0) {
                jsonObject.addProperty("nutrition", t.getNutrition());
            }
            if(t.getSaturationModifier() != 0) {
                jsonObject.addProperty("saturation_mod", t.getSaturationModifier());
            }
            if(t.isMeat()) {
                jsonObject.addProperty("meat", true);
            }
            if(t.canAlwaysEat()) {
                jsonObject.addProperty("always_eat", true);
            }
            if(t.isFastFood()) {
                jsonObject.addProperty("fast", true);
            }
            if(t.getEffects().size() > 0) {
                if(t.getEffects().size() == 1) {
                    JsonObject effect = new JsonObject();
                    effect.add("effect_in", Serializers.serialize(t.getEffects().get(0).getFirst()));
                    effect.addProperty("probability", t.getEffects().get(0).getSecond());
                    jsonObject.add("effects", effect);
                } else {
                    JsonArray effects = new JsonArray();
                    t.getEffects().forEach(pair -> {
                        JsonObject effect = new JsonObject();
                        effect.add("effect_in", Serializers.serialize(t.getEffects().get(0).getFirst()));
                        effect.addProperty("probability", t.getEffects().get(0).getSecond());
                        effects.add(effect);
                    });
                    jsonObject.add("effects", effects);
                }
            }
            return jsonObject;
        }, jsonElement -> {
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
            if(food.has("effects")) {
                if(food.get("effects").isJsonArray()) {
                    food.get("effects").getAsJsonArray().forEach(effect -> {
                        foodProperties$Builder.effect(() -> (MobEffectInstance) Serializers.getSerializer(MobEffectInstance.class).deserialize(effect.getAsJsonObject().get("effect_in").getAsJsonObject()), effect.getAsJsonObject().get("probability").getAsFloat());
                    });
                } else {
                    JsonObject effect = food.get("effects").getAsJsonObject();
                    foodProperties$Builder.effect(() -> (MobEffectInstance) Serializers.getSerializer(MobEffectInstance.class).deserialize(effect.get("effect_in").getAsJsonObject()), effect.get("probability").getAsFloat());
                }
            }
            return foodProperties$Builder.build();
        });

        register(Rarity.class, t -> {
            return new JsonPrimitive(t.toString());
        }, jsonElement -> {
            return Rarity.valueOf(jsonElement.getAsString());
        });

        register(Item.Properties.class, t -> {
            JsonObject properties = new JsonObject();

            try {
                {
                    Field field = Item.Properties.class.getDeclaredField("foodProperties");
                    boolean notAccessible = !field.canAccess(t);
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                    FoodProperties object = (FoodProperties) field.get(t);
                    if(object != null) {
                        properties.add("food", Serializers.serialize(object));
                    }
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                }
                {
                    Field field = Item.Properties.class.getDeclaredField("maxStackSize");
                    boolean notAccessible = !field.canAccess(t);
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                    int object = (int) field.get(t);
                    if(object != 64) {
                        properties.addProperty("stacks_to", object);
                    }
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                }
                // default durability?
                {
                    Field field = Item.Properties.class.getDeclaredField("maxDamage");
                    boolean notAccessible = !field.canAccess(t);
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                    int object = (int) field.get(t);
                    if(object != 0) {
                        properties.addProperty("durability", object);
                    }
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                }
                {
                    Field field = Item.Properties.class.getDeclaredField("craftingRemainingItem");
                    boolean notAccessible = !field.canAccess(t);
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                    Item object = (Item) field.get(t);
                    if(object != null) {
                        properties.add("craft_remainder", Serializers.serialize(object));
                    }
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                }
                {
                    Field field = Item.Properties.class.getDeclaredField("category");
                    boolean notAccessible = !field.canAccess(t);
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                    CreativeModeTab object = (CreativeModeTab) field.get(t);
                    if(object != null) {
                        properties.add("tab", Serializers.serialize(object));
                    }
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                }
                {
                    Field field = Item.Properties.class.getDeclaredField("rarity");
                    boolean notAccessible = !field.canAccess(t);
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                    Rarity object = (Rarity) field.get(t);
                    if(object != Rarity.COMMON) {
                        properties.add("rarity", Serializers.serialize(object));
                    }
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                }
                {
                    Field field = Item.Properties.class.getDeclaredField("isFireResistant");
                    boolean notAccessible = !field.canAccess(t);
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                    boolean object = (boolean) field.get(t);
                    if(object) {
                        properties.addProperty("fire_resistant", true);
                    }
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                }
                {
                    Field field = Item.Properties.class.getDeclaredField("canRepair");
                    boolean notAccessible = !field.canAccess(t);
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                    boolean object = (boolean) field.get(t);
                    if(!object) {
                        properties.addProperty("set_no_repair", false);
                    }
                    if (notAccessible) {
                        field.setAccessible(true);
                    }
                }
            } catch(NoSuchFieldException | IllegalAccessException exception) {
                exception.printStackTrace();
            }

            return properties;
        }, jsonElement -> {
            JsonObject properties = jsonElement.getAsJsonObject();
            Item.Properties item$properties = new Item.Properties();
            if(properties.has("food")) {
                item$properties.food((FoodProperties) Serializers.getSerializer(FoodProperties.class).deserialize(properties.get("food")));
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
                item$properties.craftRemainder((Item) Serializers.getSerializer(Item.class).deserialize(properties.get("craft_remainder")));
            }
            if(properties.has("tab")) {
                item$properties.tab((CreativeModeTab) Serializers.getSerializer(CreativeModeTab.class).deserialize(properties.get("tab")));
            }
            if(properties.has("rarity")) {
                item$properties.rarity((Rarity) Serializers.getSerializer(Rarity.class).deserialize(properties.get("rarity")));
            }
            if(properties.has("fire_resistant") && properties.get("fire_resistant").getAsBoolean()) {
                item$properties.fireResistant();
            }
            if(properties.has("set_no_repair") && properties.get("set_no_repair").getAsBoolean()) {
                item$properties.setNoRepair();
            }
            return item$properties;
        });

        register(Tier.class, t -> {
            return new JsonPrimitive(t.toString());
        }, jsonElement -> {
            return Tiers.valueOf(jsonElement.getAsString());
        });
    }

    private static <T> void register(Class<T> clazz, Function<T, JsonElement> serializer, Function<JsonElement, T> deserializer) {
        CLASS_MAP.put(clazz, new SerializerType<>(serializer, deserializer));
    }

    public static SerializerType<?> getSerializer(Class<?> clazz) {
        return CLASS_MAP.get(clazz);
    }
    public static <T> JsonElement serialize(T t) {
        return CLASS_MAP.get(t.getClass()).serialize(t);
    }
}