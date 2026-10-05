package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;

/** Shows name and health of the creature you are looking at (kept for 2 seconds). */
public class TargetHudModule extends Module {
    private LivingEntity target;
    private int tick;
    private int lastSeen;

    public TargetHudModule() {
        super("Target", "Info about the entity you are looking at (draggable)");
        makeMovable(10000, 300);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        tick++;
        if (!enabled || mc.player == null) {
            target = null;
            return;
        }
        Entity e = mc.targetedEntity;
        if (e instanceof LivingEntity le && le != mc.player) {
            target = le;
            lastSeen = tick;
        }
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        if (target == null || target.isRemoved() || tick - lastSeen > 40) return;
        TextRenderer tr = mc.textRenderer;

        String name = target.getName().getString();
        float hp = target.getHealth();
        float max = Math.max(1f, target.getMaxHealth());
        float abs = target.getAbsorptionAmount();

        int w = Math.max(120, tr.getWidth(name) + 16);
        int h = 36;
        int px = clampX(ctx, w), py = clampY(ctx, h);
        setBox(px, py, w, h);

        RenderUtil.roundedRect(ctx, px, py, w, h, 4, 0xB0101018);
        ctx.drawTextWithShadow(tr, name, px + 6, py + 5, 0xFFFFFFFF);

        float ratio = MathHelper.clamp(hp / max, 0f, 1f);
        int color = ratio > 0.5f ? 0xFF55FF55 : (ratio > 0.25f ? 0xFFFFD84D : 0xFFFF5555);
        int bx = px + 6, bw = w - 12;
        ctx.fill(bx, py + 17, bx + bw, py + 22, 0xFF333333);
        ctx.fill(bx, py + 17, bx + Math.round(bw * ratio), py + 22, color);

        String text = String.format(Locale.ROOT, "%.1f / %.1f", hp, max);
        if (abs > 0) text += String.format(Locale.ROOT, "  +%.1f", abs);
        ctx.drawTextWithShadow(tr, text, px + 6, py + 25, 0xFFDDDDDD);
    }
}
