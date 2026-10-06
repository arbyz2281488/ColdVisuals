package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;

/** Turns the vanilla hitbox display (F3+B) on and off from the GUI. */
public class HitboxesModule extends Module {
    private boolean applied;

    public HitboxesModule() { super("Hitboxes", "Shows entity hitboxes (same as F3+B)"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (enabled && !applied) {
            mc.getEntityRenderDispatcher().setRenderHitboxes(true);
            applied = true;
        } else if (!enabled && applied) {
            mc.getEntityRenderDispatcher().setRenderHitboxes(false);
            applied = false;
        }
    }
}
