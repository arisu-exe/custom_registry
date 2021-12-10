package io.github.fallOut015.custom_registry.world.effect;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

public class MobEffectInstanceSerializer {
    // TODO find out if there's a base class like JsonSerializer<MobEffectInstance> or smth. It's also possible there's already something for this in game for advancements or smth.
    public static MobEffectInstance deserialize(JsonObject jsonObject) {
        MobEffect mobEffect = Objects.requireNonNull(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(jsonObject.get("mob_effect").getAsString())));
        int duration = jsonObject.has("duration") ? jsonObject.get("duration").getAsInt() : 0;
        int amplifier = jsonObject.has("amplifier") ? jsonObject.get("amplifier").getAsInt() : 0;
        boolean ambient = jsonObject.has("ambient") && jsonObject.get("ambient").getAsBoolean();
        boolean visible = !jsonObject.has("visible") || jsonObject.get("visible").getAsBoolean();
        boolean showIcon = jsonObject.has("show_icon") ? jsonObject.get("show_icon").getAsBoolean() : visible;
        @Nullable MobEffectInstance hiddenEffect = jsonObject.has("hidden_effect") ? MobEffectInstanceSerializer.deserialize(jsonObject.get("hidden_effect").getAsJsonObject()) : null;
        return new MobEffectInstance(mobEffect, duration, amplifier, ambient, visible, showIcon, hiddenEffect);
    }
}