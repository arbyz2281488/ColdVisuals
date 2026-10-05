package com.example.visuals;

import com.example.visuals.modules.Module;
import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Main config:  .minecraft/config/visuals.properties  (auto-saved)
 * Named configs: .minecraft/config/visuals-configs/<name>.properties
 */
public class Config {
    private static final Path MAIN = FabricLoader.getInstance().getConfigDir().resolve("visuals.properties");
    private static final Path CONFIGS = FabricLoader.getInstance().getConfigDir().resolve("visuals-configs");

    public static Path configsDir() {
        try {
            Files.createDirectories(CONFIGS);
        } catch (IOException ignored) {}
        return CONFIGS;
    }

    public static boolean validName(String name) {
        return name.matches("[A-Za-z0-9_\\-]{1,32}");
    }

    // ----- main config -----
    public static void load() {
        try {
            if (Files.exists(MAIN)) read(MAIN);
        } catch (IOException ignored) {}
    }

    public static void save() {
        try {
            write(MAIN);
        } catch (IOException ignored) {}
    }

    // ----- named configs -----
    public static void saveAs(String name) throws IOException {
        write(configsDir().resolve(name + ".properties"));
    }

    public static boolean loadFrom(String name) throws IOException {
        Path file = configsDir().resolve(name + ".properties");
        if (!Files.exists(file)) return false;
        read(file);
        save();
        return true;
    }

    public static boolean delete(String name) throws IOException {
        return Files.deleteIfExists(configsDir().resolve(name + ".properties"));
    }

    public static List<String> list() {
        List<String> names = new ArrayList<>();
        try (Stream<Path> s = Files.list(configsDir())) {
            s.forEach(p -> {
                String f = p.getFileName().toString();
                if (f.endsWith(".properties")) names.add(f.substring(0, f.length() - ".properties".length()));
            });
        } catch (IOException ignored) {}
        return names;
    }

    // ----- io -----
    private static void write(Path file) throws IOException {
        Properties p = new Properties();
        for (Module m : ModuleManager.all()) {
            p.setProperty(m.name + ".enabled", String.valueOf(m.enabled));
            for (Setting s : m.settings) p.setProperty(m.name + "." + s.name, String.valueOf(s.value));
            if (m.hasActionKey) p.setProperty(m.name + ".actionKey", String.valueOf(m.actionKey));
            if (m.hasActionKey2) p.setProperty(m.name + ".actionKey2", String.valueOf(m.actionKey2));
            if (m.movable) {
                p.setProperty(m.name + ".posX", String.valueOf(m.posX));
                p.setProperty(m.name + ".posY", String.valueOf(m.posY));
            }
        }
        for (var e : BindManager.all().entrySet()) p.setProperty("bind." + e.getKey(), String.valueOf(e.getValue()));
        for (var e : CommandBinds.all().entrySet()) {
            p.setProperty("key." + e.getKey() + ".cmd", e.getValue().command());
            p.setProperty("key." + e.getKey() + ".key", String.valueOf(e.getValue().key()));
        }
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        try (var out = Files.newOutputStream(file)) {
            p.store(out, "Visuals config");
        }
    }

    private static void read(Path file) throws IOException {
        Properties p = new Properties();
        try (var in = Files.newInputStream(file)) {
            p.load(in);
        }
        for (Module m : ModuleManager.all()) {
            m.enabled = Boolean.parseBoolean(p.getProperty(m.name + ".enabled", String.valueOf(m.enabled)));
            for (Setting s : m.settings) {
                try {
                    s.value = Double.parseDouble(p.getProperty(m.name + "." + s.name, String.valueOf(s.value)));
                } catch (NumberFormatException ignored) {}
            }
            if (m.hasActionKey) {
                try {
                    m.actionKey = Integer.parseInt(p.getProperty(m.name + ".actionKey", String.valueOf(m.actionKey)));
                } catch (NumberFormatException ignored) {}
            }
            if (m.hasActionKey2) {
                try {
                    m.actionKey2 = Integer.parseInt(p.getProperty(m.name + ".actionKey2", String.valueOf(m.actionKey2)));
                } catch (NumberFormatException ignored) {}
            }
            if (m.movable) {
                try {
                    m.posX = Double.parseDouble(p.getProperty(m.name + ".posX", String.valueOf(m.posX)));
                    m.posY = Double.parseDouble(p.getProperty(m.name + ".posY", String.valueOf(m.posY)));
                } catch (NumberFormatException ignored) {}
            }
        }
        BindManager.clear();
        for (String key : p.stringPropertyNames()) {
            if (!key.startsWith("bind.")) continue;
            try {
                BindManager.set(key.substring(5), Integer.parseInt(p.getProperty(key)));
            } catch (NumberFormatException ignored) {}
        }
        CommandBinds.clear();
        for (String k : p.stringPropertyNames()) {
            if (!k.startsWith("key.") || !k.endsWith(".cmd")) continue;
            String name = k.substring(4, k.length() - 4);
            try {
                int code = Integer.parseInt(p.getProperty("key." + name + ".key", "-1"));
                if (code >= 0) CommandBinds.set(name, p.getProperty(k), code);
            } catch (NumberFormatException ignored) {}
        }
    }
}
