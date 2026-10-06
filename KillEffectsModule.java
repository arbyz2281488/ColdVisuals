package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;

import java.util.HashSet;
import java.util.Set;

/** A burst of particles where a nearby creature dies (client-side only). */
public class KillEffectsModule extends Module {
    public final Setting amount = add(Setting.number("Amount", 24, 6, 60));
    public final Setting playersOnly = add(Setting.bool("Players Only", false));

    private final Set<Integer> dead = new HashSet<>();

    public KillEffectsModule() { super("KillEffects", "Particle burst when something dies"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.world == null || mc.player == null) {
            dead.clear();
            return;
        }
        Set<Integer> present = new HashSet<>();
        var r = mc.world.getRandom();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == mc.player) continue;
            present.add(le.getId());
            if (!le.isDead() || dead.contains(le.getId())) continue;
            dead.add(le.getId());
            if (playersOnly.asBool() && !(le instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(le) > 48) continue;
            for (int i = 0; i < amount.asInt(); i++) {
                double vx = (r.nextDouble() - 0.5) * 0.5, vy = r.nextDouble() * 0.5, vz = (r.nextDouble() - 0.5) * 0.5;
                mc.world.addParticle(i % 3 == 0 ? ParticleTypes.CRIT : ParticleTypes.END_ROD,
                        le.getX(), le.getY() + le.getHeight() * 0.5, le.getZ(), vx, vy, vz);
            }
        }
        dead.retainAll(present);
    }
}
