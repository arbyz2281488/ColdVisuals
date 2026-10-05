package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;

/** Holds the sprint key for you while you walk forward (like the vanilla "toggle sprint" option). */
public class AutoSprintModule extends Module {
    private boolean held;

    public AutoSprintModule() { super("AutoSprint", "Sprints automatically while walking forward"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        if (!enabled) {
            if (held) {
                mc.options.sprintKey.setPressed(false);
                held = false;
            }
            return;
        }
        boolean want = mc.currentScreen == null && mc.options.forwardKey.isPressed() && !mc.player.isSneaking();
        if (want) {
            mc.options.sprintKey.setPressed(true);
            held = true;
        } else if (held) {
            mc.options.sprintKey.setPressed(false);
            held = false;
        }
    }
}
