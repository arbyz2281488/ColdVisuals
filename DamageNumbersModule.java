package com.example.visuals.modules;

import com.example.visuals.gui.Project;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Floating damage numbers over creatures you can see. */
public class DamageNumbersModule extends Module {
    public final Setting heal = add(Setting.bool("Show Healing", false));
    public final Setting size = add(Setting.number("Size", 1.2, 0.8, 2.5));

    private record Pop(Vec3d pos, String text, int rgb, long born) {}

    private static final long LIFE = 1200;
    private final List<Pop> pops = new ArrayList<>();
    private final Map<Integer, Float> last = new HashMap<>();

    public DamageNumbersModule() { super("DamageNumbers", "Floating damage numbers"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.world == null || mc.player == null) {
            pops.clear();
            last.clear();
            return;
        }
        long now = System.currentTimeMillis();
        Set<Integer> seen = new HashSet<>();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || le == mc.player) continue;
            if (mc.player.distanceTo(le) > 40) continue;
            float hp = le.getHealth() + le.getAbsorptionAmount();
            Float prev = last.put(le.getId(), hp);
            seen.add(le.getId());
            if (prev == null || !mc.player.canSee(le)) continue;
            Vec3d top = new Vec3d(le.getX(), le.getY() + le.getHeight() + 0.3, le.getZ());
            if (prev - hp > 0.05f) {
                pops.add(new Pop(top, String.format(Locale.ROOT, "-%.1f", prev - hp), 0xFF5555, now));
            } else if (heal.asBool() && hp - prev > 0.5f) {
                pops.add(new Pop(top, String.format(Locale.ROOT, "+%.1f", hp - prev), 0x55FF55, now));
            }
        }
        last.keySet().removeIf(id -> !seen.contains(id));
        pops.removeIf(p -> now - p.born() > LIFE);
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        long now = System.currentTimeMillis();
        float s = (float) size.value;
        for (Pop p : pops) {
            float age = (now - p.born()) / (float) LIFE;
            int[] xy = Project.toScreen(mc, ctx, p.pos().add(0, age * 0.9, 0));
            if (xy == null) continue;
            int a = Math.max(8, (int) (255 * (1f - age * age)));
            MatrixStack m = ctx.getMatrices();
            m.push();
            m.translate((float) xy[0], (float) xy[1], 0f);
            m.scale(s, s, 1f);
            ctx.drawTextWithShadow(mc.textRenderer, p.text(), -mc.textRenderer.getWidth(p.text()) / 2, -4, (a << 24) | p.rgb());
            m.pop();
        }
    }
}
