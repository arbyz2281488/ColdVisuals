package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Lists the nearest lit TNT with the time left until it explodes. */
public class TntTimerModule extends Module {
    public final Setting radius = add(Setting.number("Radius", 20, 8, 40));

    private record Line(float dist, int fuse) {}

    public TntTimerModule() {
        super("TNTTimer", "Time until lit TNT nearby explodes (draggable)");
        makeMovable(10000, 200);
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        List<Line> lines = new ArrayList<>();
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof TntEntity t) {
                float d = mc.player.distanceTo(t);
                if (d <= radius.value) lines.add(new Line(d, t.getFuse()));
            }
        }
        if (lines.isEmpty()) return;
        lines.sort(Comparator.comparingDouble(Line::dist));
        if (lines.size() > 4) lines = lines.subList(0, 4);

        TextRenderer tr = mc.textRenderer;
        int w = 96, rowH = 11;
        int h = (lines.size() + 1) * rowH + 8;
        int px = clampX(ctx, w), py = clampY(ctx, h);
        setBox(px, py, w, h);

        RenderUtil.roundedRect(ctx, px, py, w, h, 4, 0xB0101018);
        ctx.drawTextWithShadow(tr, "TNT", px + 6, py + 5, 0xFFFF6B6B);
        int ty = py + 5 + rowH;
        for (Line l : lines) {
            String left = Math.round(l.dist()) + " м";
            String right = String.format(Locale.ROOT, "%.1fс", l.fuse() / 20.0);
            ctx.drawTextWithShadow(tr, left, px + 6, ty, 0xFFDDDDDD);
            ctx.drawTextWithShadow(tr, right, px + w - 6 - tr.getWidth(right), ty, l.fuse() < 40 ? 0xFFFF5555 : 0xFFFFD27F);
            ty += rowH;
        }
    }
}
