package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;

/** Crit-style particles on the thing you hit (client-side only). */
public class HitParticlesModule extends Module {
    public final Setting amount = add(Setting.number("Amount", 8, 1, 30));

    public HitParticlesModule() { super("HitParticles", "Particles when you hit something"); }

    public void spawn(Entity target) {
        if (!enabled || target == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;
        var r = mc.world.getRandom();
        for (int i = 0; i < amount.asInt(); i++) {
            mc.world.addParticle(ParticleTypes.CRIT,
                    target.getX(), target.getBodyY(0.6), target.getZ(),
                    (r.nextDouble() - 0.5) * 0.6, r.nextDouble() * 0.4, (r.nextDouble() - 0.5) * 0.6);
        }
    }
}
