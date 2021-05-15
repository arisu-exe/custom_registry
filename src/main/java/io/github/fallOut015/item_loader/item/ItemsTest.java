package io.github.fallOut015.item_loader.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ItemsTest {
    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "item_loader");

    static void load() throws IOException {
        System.out.println("START");

        Files.walk(Paths.get("../src/main/resources/registry/item_loader/item")).filter(Files::isRegularFile).forEach(file -> {
            try {
                JsonElement jsonElement = new JsonParser().parse(new JsonReader(new FileReader(file.toFile())));

                Item item = new Item(new Item.Properties());
                jsonElement.getAsJsonObject().entrySet().forEach(entry -> {
                    if(entry.getKey() != "key") {
                        try {
                            Field property = Item.class.getDeclaredField(entry.getKey());
                            property.setAccessible(true);
                            if(property.getType() == Integer.class) {
                                property.set(item, entry.getValue().getAsInt());
                            }
                            property.setAccessible(false);
                        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                });
                //jsonElement.getAsJsonObject().entrySet().forEach(entry -> {
                //    if(entry.getKey() != "properties" || entry.getKey() != "key") {
                        // method code
                //    }
                //});

                ITEMS.register(jsonElement.getAsJsonObject().get("key").getAsString(), () -> item);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        System.out.println("END");
    }
    public static void register(IEventBus bus) {
        try {
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ITEMS.register(bus);
    }
}