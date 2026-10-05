package com.example.visuals.modules;

import com.example.visuals.BindManager;
import com.example.visuals.CommandBinds;
import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Shows the binds made with .bind (module toggles) and .key (commands). */
public class KeyBindsHudModule extends Module {
    private record Line(String left, String right, int color) {}

    public KeyBindsHudModule() {
        super("KeyBinds", "Shows your active binds (draggable)");
        makeMovable(6, 400);
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        List<Line> lines = new ArrayList<>();
        for (var e : BindManager.all().entrySet()) {
            Module m = ModuleManager.byName(e.getKey());
            boolean on = m != null && m.enabled;
            lines.add(new Line(e.getKey(), "[" + BindManager.keyName(e.getValue()) + "]",
                    on ? 0xFF66FF66 : 0xFF999999));
        }
        for (var e : CommandBinds.all().entrySet()) {
            lines.add(new Line(e.getKey() + " " + e.getValue().command(),
                    "[" + BindManager.keyName(e.getValue().key()) + "]", 0xFFFFFFFF));
        }
        if (lines.isEmpty()) return;

        TextRenderer tr = mc.textRenderer;
        int w = 90;
        for (Line l : lines) w = Math.max(w, tr.getWidth(l.left()) + tr.getWidth(l.right()) + 24);
        int rowH = 11;
        int h = (lines.size() + 1) * rowH + 8;
        int px = clampX(ctx, w), py = clampY(ctx, h);
        setBox(px, py, w, h);

        RenderUtil.roundedRect(ctx, px, py, w, h, 4, 0xB0101018);
        ctx.drawTextWithShadow(tr, "Бинды", px + 6, py + 5, 0xFFB06CFF);
        int ty = py + 5 + rowH;
        for (Line l : lines) {
            ctx.drawTextWithShadow(tr, l.left(), px + 6, ty, l.color());
            ctx.drawTextWithShadow(tr, l.right(), px + w - 6 - tr.getWidth(l.right()), ty, 0xFFDDDDDD);
            ty += rowH;
        }
    }
}
