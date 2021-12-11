package io.github.fallOut015.custom_registry.registries;

import com.google.gson.JsonElement;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public class CustomRegistry<T extends IForgeRegistryEntry<T>> {
    final String modid;
    final IForgeRegistry<T> forgeRegistry;
    final private DeferredRegister<T> deferredRegister;
    final Function<Supplier<T>, JsonElement> jsonBuilder;
    final Function<JsonElement, Supplier<T>> jsonParser;

    public CustomRegistry(final String modid, final IForgeRegistry<T> forgeRegistry, final Function<Supplier<T>, JsonElement> jsonBuilder, final Function<JsonElement, Supplier<T>> jsonParser) {
        this.modid = modid;
        this.forgeRegistry = forgeRegistry;
        this.deferredRegister = DeferredRegister.create(this.getType(), this.getModid());
        this.jsonBuilder = jsonBuilder;
        this.jsonParser = jsonParser;
    }

    public String getModid() {
        return this.modid;
    }
    public IForgeRegistry<T> getType() {
        return this.forgeRegistry;
    }
    public void register(IEventBus bus) {
        this.deferredRegister.register(bus);
    }
    public JsonElement getJsonForEntry(Supplier<T> t) {
        return this.buildJSON(t);
    }
    public void registerJson(final String name, final @Nullable JsonElement jsonElement) {
        assert jsonElement != null;
        this.deferredRegister.register(name, this.parseJSON(jsonElement));
    }
    public JsonElement buildJSON(Supplier<T> t) {
        return this.jsonBuilder.apply(t);
    }
    public Supplier<T> parseJSON(JsonElement jsonElement) {
        return this.jsonParser.apply(jsonElement);
    }
}