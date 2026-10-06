package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Colour tint over the whole screen plus dark vignette at the edges (an overlay, not a shader). */
public class ScreenTintModule extends Module {
    public final Setting tint = add(Setting.number("Tint %", 12, 0, 40));
    public final Setting r = add(Setting.number("Red", 80, 0, 255));
    public final Setting g = add(Setting.number("Green", 160, 0, 255));
    public final Setting b = add(Setting.number("Blue", 255, 0, 255));
    public final Setting vignette = add(Setting.number("Vignette %", 35, 0, 100));

    public ScreenTintModule() { super("ScreenTint", "Colour tint and vignette over the screen"); }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();

        if (tint.value > 0) {
            int a = (int) (tint.value / 100.0 * 255);
            ctx.fill(0, 0, w, h, (a << 24) | (r.asInt() << 16) | (g.asInt() << 8) | b.asInt());
        }

        if (vignette.value > 0) {
            int maxA = (int) (vignette.value / 100.0 * 200);
            int edgeY = h / 4;
            ctx.fillGradient(0, 0, w, edgeY, maxA << 24, 0);
            ctx.fillGradient(0, h - edgeY, w, h, 0, maxA << 24);
            int steps = 24, edgeX = w / 5;
            for (int i = 0; i < steps; i++) {
                int a = (int) (maxA * (1f - (float) i / steps));
                int x1 = edgeX * i / steps, x2 = edgeX * (i + 1) / steps;
                ctx.fill(x1, 0, x2, h, a << 24);
                ctx.fill(w - x2, 0, w - x1, h, a << 24);
            }
        }
    }
}
