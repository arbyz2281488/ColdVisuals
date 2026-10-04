package com.example.visuals;

import com.example.visuals.modules.Module;
import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Saves module states and settings to .minecraft/config/visuals.properties */
public class Config {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visuals.properties");

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (var in = Files.newInputStream(FILE)) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        for (Module m : ModuleManager.all()) {
            m.enabled = Boolean.parseBoolean(p.getProperty(m.name + ".enabled", String.valueOf(m.enabled)));
            for (Setting s : m.settings) {
                try {
                    s.value = Double.parseDouble(p.getProperty(m.name + "." + s.name, String.valueOf(s.value)));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Module m : ModuleManager.all()) {
            p.setProperty(m.name + ".enabled", String.valueOf(m.enabled));
            for (Setting s : m.settings) {
                p.setProperty(m.name + "." + s.name, String.valueOf(s.value));
            }
        }
        try (var out = Files.newOutputStream(FILE)) {
            p.store(out, "Visuals config");
        } catch (IOException ignored) {}
    }
}
