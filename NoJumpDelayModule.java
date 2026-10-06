package com.example.visuals.modules;

import com.example.visuals.mixin.LivingEntityAccessor;
import net.minecraft.client.MinecraftClient;

/** Removes the short delay between jumps while you hold the jump key. */
public class NoJumpDelayModule extends Module {
    public NoJumpDelayModule() { super("NoJumpDelay", "No delay between jumps while holding space"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.player == null) return;
        ((LivingEntityAccessor) mc.player).visuals$setJumpingCooldown(0);
    }
}
