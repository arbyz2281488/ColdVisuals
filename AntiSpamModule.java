package com.example.visuals.modules;

import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

/** Hides a chat message if the same text was already shown a moment ago (see Hooks). */
public class AntiSpamModule extends Module {
    public final Setting seconds = add(Setting.number("Seconds", 8, 2, 60));
    private final Map<String, Long> seen = new HashMap<>();

    public AntiSpamModule() { super("AntiSpam", "Hides repeated identical chat messages"); }

    /** Returns false if the message should be hidden. */
    public boolean allow(Text message) {
        if (!enabled) return true;
        String s = message.getString().trim();
        if (s.isEmpty()) return true;
        long now = System.currentTimeMillis();
        seen.values().removeIf(t -> now - t > 60000);
        Long last = seen.get(s);
        seen.put(s, now);
        return last == null || now - last > (long) (seconds.value * 1000);
    }
}
