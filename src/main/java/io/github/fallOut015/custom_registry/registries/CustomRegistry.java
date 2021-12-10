package io.github.fallOut015.custom_registry.registries;

import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class CustomRegistry<T extends IForgeRegistryEntry<T>> implements ICustomRegistry<T> {
    final String modid;

    public CustomRegistry(final String modid) {
        this.modid = modid;
    }

    @Override
    public String getModid() {
        return this.modid;
    }
}