package com.example.visuals.gui;

import net.minecraft.client.gui.screen.Screen;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Lets a draw handler run only once per frame per screen, even if the same handler got registered twice
 * (a screen can be rebuilt, for example when the window is resized).
 */
public final class FrameGuard {
    private static final Map<Screen, Map<String, Long>> LAST = new WeakHashMap<>();

    private FrameGuard() {}

    public static boolean once(Screen s, String key) {
        long now = System.nanoTime();
        Map<String, Long> m = LAST.computeIfAbsent(s, k -> new HashMap<>());
        Long prev = m.get(key);
        if (prev != null && now - prev < 500_000L) return false; // same frame: a duplicate handler
        m.put(key, now);
        return true;
    }
}
