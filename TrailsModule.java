package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;

/** Glowing particle trail behind you while moving (client-side only). */
public class TrailsModule extends Module {
    public final Setting amount = add(Setting.number("Amount", 2, 1, 6));

    public TrailsModule() { super("Trails", "Glowing trail behind you while moving"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.player == null || mc.world == null) return;
        if (mc.player.getVelocity().horizontalLength() < 0.04) return;
        var r = mc.world.getRandom();
        for (int i = 0; i < amount.asInt(); i++) {
            mc.world.addParticle(ParticleTypes.END_ROD,
                    mc.player.getX() + (r.nextDouble() - 0.5) * 0.4,
                    mc.player.getY() + 0.1 + r.nextDouble() * 0.3,
                    mc.player.getZ() + (r.nextDouble() - 0.5) * 0.4,
                    0, 0.01, 0);
        }
    }
}
