package com.example.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/** Friend list stored in .minecraft/config/visuals-friends.txt (one nickname per line). */
public class FriendManager {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visuals-friends.txt");
    private static final Set<String> FRIENDS = new LinkedHashSet<>();
    private static boolean loaded;

    public static Set<String> all() {
        ensureLoaded();
        return FRIENDS;
    }

    public static boolean add(String name) {
        ensureLoaded();
        for (String f : FRIENDS) if (f.equalsIgnoreCase(name)) return false;
        FRIENDS.add(name);
        save();
        return true;
    }

    public static boolean remove(String name) {
        ensureLoaded();
        boolean removed = FRIENDS.removeIf(f -> f.equalsIgnoreCase(name));
        if (removed) save();
        return removed;
    }

    public static boolean isFriend(String name) {
        ensureLoaded();
        for (String f : FRIENDS) if (f.equalsIgnoreCase(name)) return true;
        return false;
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try {
            if (Files.exists(FILE)) {
                for (String line : Files.readAllLines(FILE)) {
                    if (!line.isBlank()) FRIENDS.add(line.trim());
                }
            }
        } catch (IOException ignored) {}
    }

    private static void save() {
        try {
            Files.write(FILE, FRIENDS);
        } catch (IOException ignored) {}
    }
}
