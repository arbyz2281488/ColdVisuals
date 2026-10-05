package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class HudModule extends Module {
    public final Setting fps = add(Setting.bool("Show FPS", true));
    public final Setting coords = add(Setting.bool("Show Coords", true));
    public final Setting direction = add(Setting.bool("Show Direction", true));
    public final Setting scale = add(Setting.number("Scale", 1.0, 0.5, 2.5));

    public HudModule() {
        super("HUD", "Small info overlay (draggable)");
        makeMovable(6, 26);
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        List<String> lines = new ArrayList<>();
        if (fps.asBool()) lines.add("FPS: " + mc.getCurrentFps());
        if (coords.asBool()) {
            lines.add(String.format("XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
        if (direction.asBool()) lines.add("Facing: " + mc.player.getHorizontalFacing().asString());
        if (lines.isEmpty()) return;

        TextRenderer tr = mc.textRenderer;
        float s = (float) scale.value;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, tr.getWidth(l));
        int w = Math.round(maxW * s) + 2;
        int h = Math.round(lines.size() * 10 * s);
        int px = clampX(ctx, w), py = clampY(ctx, h);
        setBox(px, py, w, h);

        ctx.getMatrices().push();
        ctx.getMatrices().scale(s, s, 1f);
        int tx = Math.round(px / s), ty = Math.round(py / s);
        for (String line : lines) {
            ctx.drawTextWithShadow(tr, line, tx, ty, 0xFFFFFFFF);
            ty += 10;
        }
        ctx.getMatrices().pop();
    }
}
