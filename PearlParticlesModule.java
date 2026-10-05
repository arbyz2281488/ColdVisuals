package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.particle.ParticleTypes;

/** Particles that follow flying ender pearls (client-side only). */
public class PearlParticlesModule extends Module {
    public final Setting amount = add(Setting.number("Amount", 3, 1, 10));

    public PearlParticlesModule() { super("PearlParticles", "Particle trail on ender pearls"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.world == null) return;
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof EnderPearlEntity) {
                for (int i = 0; i < amount.asInt(); i++) {
                    mc.world.addParticle(ParticleTypes.PORTAL, e.getX(), e.getY() + 0.1, e.getZ(), 0, 0, 0);
                }
            }
        }
    }
}
