package com.example.visuals;

import com.example.visuals.mixin.MinecraftClientAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Saved nicknames for offline-mode servers (the same kind of account a launcher like TLauncher creates).
 * Switching changes the name the game uses the next time you connect to a server.
 * Stored in .minecraft/config/visuals-accounts.txt (one nickname per line).
 */
public class AccountManager {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visuals-accounts.txt");
    private static final Set<String> NAMES = new LinkedHashSet<>();
    private static boolean loaded;

    public static boolean validName(String name) {
        return name.matches("[A-Za-z0-9_]{3,16}");
    }

    public static Set<String> all() {
        ensureLoaded();
        return NAMES;
    }

    public static boolean add(String name) {
        ensureLoaded();
        for (String n : NAMES) if (n.equalsIgnoreCase(name)) return false;
        NAMES.add(name);
        save();
        return true;
    }

    public static void remove(String name) {
        ensureLoaded();
        if (NAMES.removeIf(n -> n.equalsIgnoreCase(name))) save();
    }

    public static String randomName() {
        Random r = new Random();
        return "Cold_" + (1000 + r.nextInt(9000));
    }

    public static String current() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }

    /** Switches to an offline account with this nickname. */
    public static void use(String name) {
        UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        Session s = new Session(name, id, "0", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).visuals$setSession(s);
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try {
            if (Files.exists(FILE)) {
                for (String line : Files.readAllLines(FILE)) {
                    if (validName(line.trim())) NAMES.add(line.trim());
                }
            }
        } catch (IOException ignored) {}
    }

    private static void save() {
        try {
            Files.write(FILE, NAMES);
        } catch (IOException ignored) {}
    }
}
