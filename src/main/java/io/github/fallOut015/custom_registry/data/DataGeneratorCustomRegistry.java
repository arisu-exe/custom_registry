package io.github.fallOut015.custom_registry.data;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneratorCustomRegistry {
    @SubscribeEvent
    public static void gatherData(final GatherDataEvent event) {
        @SuppressWarnings("unused")
        DataGenerator gen = event.getGenerator();

        gen.addProvider(new RegistryObjectProvider<Item>(gen, ForgeRegistries.ITEMS.getRegistryName()) {
            @Override
            public void addRegistryObjects() {
                Registry.ITEM.forEach(this::addRegistryObject);
            }
        });
    }
}