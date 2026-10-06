package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;

/** Fixes the time of day on your screen only (the server's real time is not changed). */
public class TimeChangerModule extends Module {
    /** In-game clock hour: 0 = midnight, 6 = sunrise, 12 = noon, 18 = sunset. */
    public final Setting hour = add(Setting.number("Hour", 12, 0, 24));

    public TimeChangerModule() { super("TimeChanger", "Fixed time of day, only for you"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.world == null) return;
        // tick 0 = 06:00, 6000 = 12:00, 12000 = 18:00, 18000 = 00:00
        long ticks = (long) (((hour.value - 6 + 24) % 24) * 1000);
        mc.world.getLevelProperties().setTimeOfDay(ticks);
    }
}
