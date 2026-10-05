package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;

/** Expanding ring of particles under you when you jump (client-side only). */
public class JumpCircleModule extends Module {
    public final Setting radius = add(Setting.number("Radius", 1.2, 0.5, 3.0));
    private boolean wasOnGround = true;

    public JumpCircleModule() { super("JumpCircle", "Particle ring when you jump"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        boolean ground = mc.player.isOnGround();
        if (enabled && wasOnGround && !ground && mc.player.getVelocity().y > 0) {
            int n = 28;
            double speed = radius.value * 0.06;
            for (int i = 0; i < n; i++) {
                double a = i * 2 * Math.PI / n;
                mc.world.addParticle(ParticleTypes.END_ROD,
                        mc.player.getX(), mc.player.getY() + 0.05, mc.player.getZ(),
                        Math.cos(a) * speed, 0.0, Math.sin(a) * speed);
            }
        }
        wasOnGround = ground;
    }
}
