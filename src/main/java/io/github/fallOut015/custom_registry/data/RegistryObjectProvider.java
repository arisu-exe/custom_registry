package io.github.fallOut015.custom_registry.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.fallOut015.custom_registry.MainCustomRegistry;
import io.github.fallOut015.custom_registry.registries.CustomRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class RegistryObjectProvider<T extends IForgeRegistryEntry<T>> implements DataProvider {
    private static final Gson GSON;

    static {
        GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    }

    private final DataGenerator gen;
    private final ResourceLocation registry;
    private final Map<Supplier<T>, JsonElement> data;

    RegistryObjectProvider(DataGenerator gen, final ResourceLocation registry) {
        this.gen = gen;
        this.registry = registry;
        this.data = new HashMap<>();
    }

    public abstract void addRegistryObjects();

    @Override
    public void run(HashCache cache) throws IOException {
        this.addRegistryObjects();
        this.data.forEach((registryObject, jsonElement) -> {
            Path target;
            target = this.gen.getOutputFolder().resolve("registry/" + Objects.requireNonNull(registryObject.get().getRegistryName()).getNamespace() + "/" + this.registry.getPath() + "/" + Objects.requireNonNull(registryObject.get().getRegistryName()).getPath() + ".json");
            try {
                DataProvider.save(GSON, cache, jsonElement, target);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @Override
    public String getName() {
        return "Registry Objects: " + this.registry.getPath();
    }

    public void addRegistryObject(T t) {
        this.data.put(() -> t, ((CustomRegistry<T>) MainCustomRegistry.getOrMakeCustomRegistry(this.registry, Objects.requireNonNull(t.getRegistryName()).getNamespace())).buildJSON(() -> t));
    }
}
