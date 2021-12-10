package io.github.fallOut015.custom_registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.github.fallOut015.custom_registry.registries.ICustomRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryManager;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/*
Registry packs are loaded server-side and client-side, so they are put in the local .minecraft folder and the world folder
 */

@Mod(MainCustomRegistry.MODID)
public class MainCustomRegistry {
    private static final List<ICustomRegistry<?>> CUSTOM_REGISTRIES;
    public static final String MODID = "custom_registry";

    static {
        CUSTOM_REGISTRIES = new LinkedList<>();
    }

    public MainCustomRegistry() {
        File registryPack = new File("../run/registry");
        @Nullable String[] namespaces = registryPack.list((current, name) -> new File(current, name).isDirectory());
        if(namespaces != null) {
            for(String namespace : namespaces) {
                @Nullable String[] registries = new File(namespace).list((current, name) -> new File(current, name).isDirectory());
                if(registries != null) {
                    for(String registry : registries) {
                        ICustomRegistry<?> customRegistry;
                        boolean registryExists = CUSTOM_REGISTRIES.stream().anyMatch(iCustomRegistry -> iCustomRegistry.getModid().equals(namespace) && iCustomRegistry.getType().equals(RegistryManager.ACTIVE.getRegistry(new ResourceLocation(registry))));
                        if(registryExists) {
                            customRegistry = CUSTOM_REGISTRIES.stream().filter(iCustomRegistry -> iCustomRegistry.getModid().equals(namespace) && iCustomRegistry.getType().equals(RegistryManager.ACTIVE.getRegistry(new ResourceLocation(registry)))).collect(Collectors.toList()).get(0);
                        } else {
                            customRegistry = ICustomRegistry.constructGet(new ResourceLocation(registry), namespace);
                        }
                        try {
                            Files.walk(Paths.get(registry)).filter(Files::isRegularFile).forEach(path -> {
                                JsonElement jsonElement = null;
                                try {
                                    jsonElement = new JsonParser().parse(new JsonReader(new FileReader(path.toFile())));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                customRegistry.registerJson(FilenameUtils.removeExtension(path.getFileName().toString()), jsonElement);
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        CUSTOM_REGISTRIES.forEach(deferredRegister -> deferredRegister.register(FMLJavaModLoadingContext.get().getModEventBus()));

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
}