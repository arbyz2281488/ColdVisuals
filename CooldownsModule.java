package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Panel: active potion effects and item cooldowns with the time left (draggable).
 * Only cooldowns that the game shows on the item itself can be read.
 */
public class CooldownsModule extends Module {
    public final Setting effects = add(Setting.bool("Show Effects", true));
    public final Setting cooldowns = add(Setting.bool("Show Cooldowns", true));

    private static final int ACCENT = 0xFFB06CFF;

    private static class Track {
        float last;
        float totalTicks = 0;
        float remainingTicks = -1;
    }

    private record Line(String left, String right, boolean header) {}

    private final Map<Item, Track> tracks = new HashMap<>();

    public CooldownsModule() {
        super("Cooldowns", "Panel with effects and cooldowns and time left (draggable)");
        makeMovable(6, 80);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.player == null) {
            tracks.clear();
            return;
        }
        var mgr = mc.player.getItemCooldownManager();
        Set<Item> seen = new HashSet<>();

        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < 36; i++) stacks.add(mc.player.getInventory().getStack(i));
        stacks.add(mc.player.getOffHandStack());

        for (ItemStack s : stacks) {
            if (s.isEmpty()) continue;
            Item item = s.getItem();
            if (!seen.add(item)) continue;
            float p = mgr.getCooldownProgress(s, 0f);
            if (p <= 0f) {
                tracks.remove(item);
                seen.remove(item);
                continue;
            }
            Track t = tracks.get(item);
            if (t == null) {
                t = new Track();
                t.last = p;
                tracks.put(item, t);
            } else {
                if (t.totalTicks <= 0 && t.last - p > 0.0001f) t.totalTicks = 1f / (t.last - p);
                t.last = p;
            }
            t.remainingTicks = t.totalTicks > 0 ? p * t.totalTicks : -1;
        }
        tracks.keySet().removeIf(i -> !seen.contains(i));
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        List<Line> lines = new ArrayList<>();

        if (effects.asBool()) {
            List<Line> list = new ArrayList<>();
            for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                String name = e.getEffectType().value().getName().getString();
                if (e.getAmplifier() > 0) name += " " + (e.getAmplifier() + 1);
                String time = e.isInfinite() ? "--" : fmt(e.getDuration());
                list.add(new Line(name, time, false));
            }
            if (!list.isEmpty()) {
                lines.add(new Line("Эффекты", "", true));
                lines.addAll(list);
            }
        }

        if (cooldowns.asBool()) {
            List<Line> list = new ArrayList<>();
            for (Map.Entry<Item, Track> en : tracks.entrySet()) {
                Track t = en.getValue();
                String time = t.remainingTicks >= 0 ? fmt(Math.round(t.remainingTicks)) : "...";
                list.add(new Line(en.getKey().getName().getString(), time, false));
            }
            if (!list.isEmpty()) {
                lines.add(new Line("КД", "", true));
                lines.addAll(list);
            }
        }

        if (lines.isEmpty()) return;

        TextRenderer tr = mc.textRenderer;
        int w = 110;
        for (Line l : lines) w = Math.max(w, tr.getWidth(l.left()) + tr.getWidth(l.right()) + 28);
        int rowH = 11;
        int h = lines.size() * rowH + 8;
        int px = clampX(ctx, w), py = clampY(ctx, h);
        setBox(px, py, w, h);

        RenderUtil.roundedRect(ctx, px, py, w, h, 4, 0xB0101018);
        int ty = py + 5;
        for (Line l : lines) {
            if (l.header()) {
                ctx.drawTextWithShadow(tr, l.left(), px + 6, ty, ACCENT);
            } else {
                ctx.drawTextWithShadow(tr, l.left(), px + 6, ty, 0xFFFFFFFF);
                ctx.drawTextWithShadow(tr, l.right(), px + w - 6 - tr.getWidth(l.right()), ty, 0xFFFFD27F);
            }
            ty += rowH;
        }
    }

    private static String fmt(int ticks) {
        int sec = ticks / 20;
        if (sec >= 60) return String.format(Locale.ROOT, "%d:%02d", sec / 60, sec % 60);
        if (ticks < 200) return String.format(Locale.ROOT, "%.1fс", ticks / 20.0);
        return sec + "с";
    }
}
