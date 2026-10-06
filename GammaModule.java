package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/** Full brightness on your screen: a client-side night vision effect (the server is not told about it). */
public class GammaModule extends Module {
    private boolean applied;

    public GammaModule() { super("Gamma", "Full brightness everywhere (client-side night vision)"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null) {
            applied = false;
            return;
        }
        if (!enabled) {
            if (applied) {
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                applied = false;
            }
            return;
        }
        StatusEffectInstance cur = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (cur == null || (applied && cur.getDuration() < 700)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
            applied = true;
        }
    }
}
