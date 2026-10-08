package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;

/** Bubble burst on the thing you hit (client-side only). */
public class HitBubblesModule extends Module {
    public final Setting amount = add(Setting.number("Amount", 10, 3, 30));

    public HitBubblesModule() { super("HitBubbles", "Bubbles when you hit something"); }

    public void spawn(Entity target) {
        if (!enabled || target == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;
        var r = mc.world.getRandom();
        for (int i = 0; i < amount.asInt(); i++) {
            mc.world.addParticle(ParticleTypes.BUBBLE_POP,
                    target.getX() + (r.nextDouble() - 0.5) * target.getWidth(),
                    target.getBodyY(0.3 + r.nextDouble() * 0.6),
                    target.getZ() + (r.nextDouble() - 0.5) * target.getWidth(),
                    (r.nextDouble() - 0.5) * 0.15, r.nextDouble() * 0.15, (r.nextDouble() - 0.5) * 0.15);
        }
    }
}
