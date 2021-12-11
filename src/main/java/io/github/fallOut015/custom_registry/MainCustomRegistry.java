package io.github.fallOut015.custom_registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.github.fallOut015.custom_registry.registries.CustomRegistries;
import io.github.fallOut015.custom_registry.registries.CustomRegistry;
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

@Mod(MainCustomRegistry.MODID)
public class MainCustomRegistry {
    private static final List<CustomRegistry<?>> CUSTOM_REGISTRIES;
    public static final String MODID = "custom_registry";

    static {
        CUSTOM_REGISTRIES = new LinkedList<>();
    }

    public MainCustomRegistry() {
        System.out.println("Loading registry packs...");
        File clientRegistry = new File("registrypacks");
        @Nullable String[] registryPacks = clientRegistry.list((current, name) -> new File(current, name).isDirectory());
        if(registryPacks != null) {
            for(String registryPack : registryPacks) {
                JsonElement pack = null;
                try {
                    pack = new JsonParser().parse(new JsonReader(new FileReader("registrypacks/" + registryPack + "/pack.mcmeta")));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (pack != null) {
                    System.out.println("\tLoading registry pack " + registryPack + ": " + pack.getAsJsonObject().get("pack").getAsJsonObject().get("description").getAsString());
                } else {
                    System.err.println("missing pack.mcmeta");
                }
                @Nullable String[] namespaces = new File("registrypacks/" + registryPack).list((current, name) -> new File(current, name).isDirectory());
                if(namespaces != null) {
                    for(String namespace : namespaces) {
                        System.out.println("\t\tLoading namespace " + namespace);
                        @Nullable String[] registries = new File("registrypacks/" + registryPack + "/" + namespace).list((current, name) -> new File(current, name).isDirectory());
                        if(registries != null) {
                            for(String registry : registries) {
                                System.out.println("\t\t\tLoading registry " + registry);
                                CustomRegistry<?> customRegistry;
                                boolean registryExists = CUSTOM_REGISTRIES.stream().anyMatch(iCustomRegistry -> iCustomRegistry.getModid().equals(namespace) && iCustomRegistry.getType().equals(RegistryManager.ACTIVE.getRegistry(new ResourceLocation(registry))));
                                if(registryExists) {
                                    customRegistry = CUSTOM_REGISTRIES.stream().filter(iCustomRegistry -> iCustomRegistry.getModid().equals(namespace) && iCustomRegistry.getType().equals(RegistryManager.ACTIVE.getRegistry(new ResourceLocation(registry)))).collect(Collectors.toList()).get(0);
                                } else {
                                    customRegistry = CustomRegistries.constructGet(new ResourceLocation(registry), namespace);
                                    CUSTOM_REGISTRIES.add(customRegistry);
                                }
                                try {
                                    Files.walk(Paths.get("registrypacks/" + registryPack + "/" + namespace + "/" + registry)).filter(Files::isRegularFile).forEach(path -> {
                                        JsonElement jsonElement = null;
                                        try {
                                            jsonElement = new JsonParser().parse(new JsonReader(new FileReader(path.toFile())));
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                        System.out.println("\t\t\t\tRegistering " + namespace + ":" + FilenameUtils.removeExtension(path.getFileName().toString()));
                                        customRegistry.registerJson(FilenameUtils.removeExtension(path.getFileName().toString()), jsonElement);
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
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