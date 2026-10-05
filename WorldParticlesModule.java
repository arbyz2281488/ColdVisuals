package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;

/** Floating particles around you (client-side only). */
public class WorldParticlesModule extends Module {
    public final Setting amount = add(Setting.number("Amount", 3, 1, 10));
    public final Setting radius = add(Setting.number("Radius", 8, 3, 16));

    public WorldParticlesModule() { super("WorldParticles", "Floating particles around you"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.player == null || mc.world == null) return;
        var r = mc.world.getRandom();
        double rad = radius.value;
        for (int i = 0; i < amount.asInt(); i++) {
            mc.world.addParticle(ParticleTypes.END_ROD,
                    mc.player.getX() + (r.nextDouble() * 2 - 1) * rad,
                    mc.player.getY() + r.nextDouble() * rad * 0.6,
                    mc.player.getZ() + (r.nextDouble() * 2 - 1) * rad,
                    0, -0.01, 0);
        }
    }
}
