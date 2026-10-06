package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;

/**
 * Marks the creature you just hit with a rotating ring of particles around it (and a halo over its head).
 * Particles are drawn by the game like any other, and the marker is only shown while you can actually see
 * the target, so it is a highlight, not an X-ray.
 */
public class TargetEspModule extends Module {
    public final Setting count = add(Setting.number("Particles", 14, 6, 30));
    public final Setting halo = add(Setting.bool("Halo Over Head", true));

    private Entity target;
    private long until;

    public TargetEspModule() { super("TargetESP", "Marks the target you are fighting"); }

    public void setTarget(Entity e) {
        if (!enabled || !(e instanceof LivingEntity)) return;
        target = e;
        until = System.currentTimeMillis() + 5000;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || target == null || mc.world == null || mc.player == null) return;
        if (System.currentTimeMillis() > until || target.isRemoved()
                || (target instanceof LivingEntity le && le.isDead())) {
            target = null;
            return;
        }
        if (!mc.player.canSee(target)) return;

        double spin = (mc.world.getTime() % 40) / 40.0 * Math.PI * 2;
        int n = count.asInt();
        double r = target.getWidth() * 0.9 + 0.2;
        for (int i = 0; i < n; i++) {
            double a = spin + i * Math.PI * 2 / n;
            double dx = Math.cos(a) * r, dz = Math.sin(a) * r;
            mc.world.addParticle(ParticleTypes.END_ROD, target.getX() + dx, target.getY() + 0.1, target.getZ() + dz, 0, 0, 0);
            if (halo.asBool()) {
                mc.world.addParticle(ParticleTypes.END_ROD, target.getX() + dx * 0.6,
                        target.getY() + target.getHeight() + 0.25, target.getZ() + dz * 0.6, 0, 0, 0);
            }
        }
    }
}
