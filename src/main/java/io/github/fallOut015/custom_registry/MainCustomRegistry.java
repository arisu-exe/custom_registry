package io.github.fallOut015.custom_registry;

import com.google.common.collect.BiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/*
Custom registries are part of the .minecraft folder
Addon pack .zips contain registry info, resources, and data for custom additions
 */

@Mod(MainCustomRegistry.MODID)
public class MainCustomRegistry {
    private static final List<DeferredRegister<?>> all_registries;
    public static final String MODID = "custom_registry";

    static {
        all_registries = new LinkedList<>();
    }

    public MainCustomRegistry() {
        // TODO defer this to when actual registration takes place

        File run_registry = new File("../run/registry"); // the world's registry folder
        String[] namespaces = run_registry.list((current, name) -> new File(current, name).isDirectory()); // all the namespaces contained
        if(namespaces != null) {
            for(String namespace : namespaces) {
                String[] registry_types = new File(namespace).list((current, name) -> new File(current, name).isDirectory()); // all the registry types contains
                if(registry_types != null) {
                    for(String registry_type : registry_types) {
                        try {
                            Files.walk(Paths.get(registry_type)).filter(Files::isRegularFile).forEach(file -> { // all the registry objects
                                try {
                                    JsonElement jsonElement = new JsonParser().parse(new JsonReader(new FileReader(file.toFile())));

                                    ResourceLocation registry_name = new ResourceLocation(registry_type);
                                    ForgeRegistry<?> registry = RegistryManager.ACTIVE.getRegistry(registry_name);

                                    try {
                                        Field superTypes = RegistryManager.class.getDeclaredField("superTypes");
                                        superTypes.setAccessible(true);
                                        BiMap<Class<? extends IForgeRegistryEntry<?>>, ResourceLocation> _superTypes = (BiMap<Class<? extends IForgeRegistryEntry<?>>, ResourceLocation>) superTypes.get(RegistryManager.ACTIVE);
                                        superTypes.setAccessible(false);
                                        Class<? extends IForgeRegistryEntry<?>> objectType = _superTypes.inverse().get(registry_name);
                                        Object registryObject = objectType.cast(new Object());

                                        jsonElement.getAsJsonObject().entrySet().forEach(entry -> {
                                            if(!entry.getKey().equals("name")) {
                                                try {
                                                    Field property = Item.class.getDeclaredField(entry.getKey());
                                                    property.setAccessible(true);
                                                    if(property.getType() == Integer.class) {
                                                        property.set(registryObject, entry.getValue().getAsInt());
                                                    }
                                                    property.setAccessible(false);
                                                } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                        Predicate<DeferredRegister<?>> matchesAll = deferredRegister -> {
                                            try {
                                                Field modid = deferredRegister.getClass().getDeclaredField("modid");
                                                modid.setAccessible(true);
                                                String _modid = (String) modid.get(deferredRegister);
                                                modid.setAccessible(false);

                                                Field type = deferredRegister.getClass().getDeclaredField("type");
                                                type.setAccessible(true);
                                                IForgeRegistry<?> _type = (IForgeRegistry<?>) type.get(deferredRegister);
                                                type.setAccessible(false);

                                                return _modid.equals(namespace) && _type.equals(registry);
                                            } catch(IllegalAccessException | NoSuchFieldException exception) {
                                                exception.printStackTrace();
                                                return false;
                                            }
                                        };

                                        if(all_registries.stream().noneMatch(matchesAll)) {
                                            all_registries.add(DeferredRegister.create((IForgeRegistry<?>) registry, namespace));
                                        }

                                        DeferredRegister<?> deferredRegister = all_registries.stream().filter(matchesAll).collect(Collectors.toList()).get(0);
                                        deferredRegister.register(jsonElement.getAsJsonObject().get("name").getAsString(), () -> registryObject);
                                    } catch(IllegalAccessException | NoSuchFieldException exception) {
                                        exception.printStackTrace();
                                    }
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }
    private void enqueueIMC(final InterModEnqueueEvent event) {
    }
    private void processIMC(final InterModProcessEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
    }
}