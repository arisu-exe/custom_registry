package io.github.fallOut015.custom_registry.registries;

import com.google.gson.JsonObject;
import io.github.fallOut015.custom_registry.world.effect.MobEffectInstanceSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.ForgeRegistries;

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
            Item.Properties properties = new Item.Properties();
            if(object.has("food")) {
                FoodProperties.Builder foodProperties$Builder = new FoodProperties.Builder();
                JsonObject food = object.getAsJsonObject("food");
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
                    foodProperties$Builder.effect(() -> MobEffectInstanceSerializer.deserialize(effect.get("effect_in").getAsJsonObject()), effect.get("probability").getAsFloat());
                }
                properties.food(foodProperties$Builder.build());
            }
            if(object.has("stacks_to")) {
                properties.stacksTo(object.get("stacks_to").getAsInt());
            }
            if(object.has("default_durability")) {
                properties.defaultDurability(object.get("default_durability").getAsInt());
            }
            if(object.has("durability")) {
                properties.durability(object.get("durability").getAsInt());
            }
            if(object.has("craft_remainder")) {
                properties.craftRemainder(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(object.get("craft_remainder").getAsString()))));
            }
            if(object.has("tab")) {
                // TODO (replace with cross-mod capable code eventually)
            /*properties.tab(Arrays.stream(CreativeModeTab.TABS).filter(creativeModeTab -> {
                try {
                    Field langId = CreativeModeTab.class.getDeclaredField("langId");
                    boolean notAccessible = !langId.canAccess(creativeModeTab);
                    if(notAccessible) {
                        langId.setAccessible(true);
                    }
                    String ret = (String) langId.get(creativeModeTab);
                    if(notAccessible) {
                        langId.setAccessible(true);
                    }
                    if(ret.equals(object.get("tag").getAsString())) {
                        return true;
                    }
                } catch(NoSuchFieldException | IllegalAccessException exception) {
                    exception.printStackTrace();
                }
                return false;
            }).collect(Collectors.toList()).get(0));*/
            }
            if(object.has("rarity")) {
                properties.rarity(Rarity.valueOf(object.get("rarity").getAsString()));
            }
            if(object.has("fire_resistant") && object.get("fire_resistant").getAsBoolean()) {
                properties.fireResistant();
            }
            if(object.has("set_no_repair") && object.get("set_no_repair").getAsBoolean()) {
                properties.setNoRepair();
            }

            return () -> new Item(properties);
        }));
    }

    public static CustomRegistry<?> constructGet(ResourceLocation registry, String namespace) {
        return TYPE_MAP.get(registry).apply(namespace);
    }
}