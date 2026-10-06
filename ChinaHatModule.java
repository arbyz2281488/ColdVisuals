package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;

/** A hat made of glowing particles above your head (and above other Cold users). Client-side only. */
public class ChinaHatModule extends Module {
    public final Setting density = add(Setting.number("Density", 3, 1, 8));
    public final Setting others = add(Setting.bool("On Cold Users", true));

    public ChinaHatModule() { super("ChinaHat", "Glowing particle hat above heads"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.world == null || mc.player == null) return;
        var r = mc.world.getRandom();
        for (PlayerEntity p : mc.world.getPlayers()) {
            boolean self = p == mc.player;
            if (!self && !(others.asBool() && ModuleManager.COLDTAG.isColdUser(p.getName().getString()))) continue;
            if (mc.player.distanceTo(p) > 48) continue;
            double baseY = p.getY() + p.getHeight() + 0.03;
            for (int i = 0; i < density.asInt(); i++) {
                int ring = r.nextInt(4);
                double radius = 0.62 * (1.0 - ring / 4.0);
                double a = r.nextDouble() * Math.PI * 2;
                mc.world.addParticle(ParticleTypes.END_ROD,
                        p.getX() + Math.cos(a) * radius, baseY + ring * 0.08, p.getZ() + Math.sin(a) * radius, 0, 0, 0);
            }
        }
    }
}
