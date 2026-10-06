package com.example.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Keyboard binds that run a chat command (like /spawn) when the key is pressed. Managed with .key */
public class CommandBinds {
    public record Bind(String command, int key) {}

    private static final Map<String, Bind> BINDS = new LinkedHashMap<>(); // label -> bind
    private static final Map<String, Boolean> DOWN = new HashMap<>();

    public static Map<String, Bind> all() { return BINDS; }

    public static void set(String name, String command, int key) { BINDS.put(name, new Bind(command, key)); }

    public static void remove(String name) { BINDS.remove(name); DOWN.remove(name); }

    public static void clear() { BINDS.clear(); DOWN.clear(); }

    public static void tick(MinecraftClient mc) {
        if (mc.getWindow() == null || BINDS.isEmpty()) return;
        long handle = mc.getWindow().getHandle();
        for (Map.Entry<String, Bind> e : BINDS.entrySet()) {
            boolean down = InputUtil.isKeyPressed(handle, e.getValue().key());
            boolean was = DOWN.getOrDefault(e.getKey(), false);
            if (down && !was && mc.currentScreen == null && mc.player != null) {
                run(mc, e.getValue().command());
            }
            DOWN.put(e.getKey(), down);
        }
    }

    public static void run(MinecraftClient mc, String cmd) {
        if (cmd.startsWith(".")) { // our own mod command, e.g. ".config load pvp"
            CommandHandler.handle(cmd);
            return;
        }
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null) return;
        if (cmd.startsWith("/")) handler.sendChatCommand(cmd.substring(1));
        else handler.sendChatMessage(cmd);
    }
}
