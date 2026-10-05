package com.example.visuals;

import com.example.visuals.modules.Module;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Keyboard binds: key -> toggle a module. Managed with the .bind chat command. */
public class BindManager {
    private static final Map<String, Integer> BINDS = new LinkedHashMap<>(); // module name -> GLFW key code
    private static final Map<String, Boolean> DOWN = new HashMap<>();

    public static Map<String, Integer> all() { return BINDS; }

    public static void set(String module, int key) { BINDS.put(module, key); }

    public static void remove(String module) { BINDS.remove(module); }

    public static void clear() { BINDS.clear(); DOWN.clear(); }

    public static void tick(MinecraftClient mc) {
        if (mc.getWindow() == null || BINDS.isEmpty()) return;
        long handle = mc.getWindow().getHandle();
        for (Map.Entry<String, Integer> e : BINDS.entrySet()) {
            boolean down = InputUtil.isKeyPressed(handle, e.getValue());
            boolean was = DOWN.getOrDefault(e.getKey(), false);
            if (down && !was && mc.currentScreen == null && mc.player != null) {
                Module m = ModuleManager.byName(e.getKey());
                if (m != null) {
                    m.toggle();
                    mc.player.sendMessage(Text.literal(m.name + (m.enabled ? " §aON" : " §cOFF")), true);
                }
            }
            DOWN.put(e.getKey(), down);
        }
    }

    public static String keyName(int code) {
        return InputUtil.Type.KEYSYM.createFromCode(code).getLocalizedText().getString();
    }

    /** Parses names like R, F5, RSHIFT, SPACE, NUMPAD1. Returns -1 if unknown. */
    public static int parseKey(String input) {
        String n = input.toLowerCase(Locale.ROOT);
        switch (n) {
            case "rshift" -> n = "right.shift";
            case "lshift", "shift" -> n = "left.shift";
            case "rctrl" -> n = "right.control";
            case "lctrl", "ctrl" -> n = "left.control";
            case "ralt" -> n = "right.alt";
            case "lalt", "alt" -> n = "left.alt";
            case "pageup" -> n = "page.up";
            case "pagedown" -> n = "page.down";
            case "capslock" -> n = "caps.lock";
            default -> {
                if (n.startsWith("numpad") && n.length() == 7) n = "keypad." + n.charAt(6);
            }
        }
        try {
            return InputUtil.fromTranslationKey("key.keyboard." + n).getCode();
        } catch (IllegalArgumentException ex) {
            return -1;
        }
    }
}
