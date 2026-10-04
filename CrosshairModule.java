package com.example.visuals.modules;

import net.minecraft.client.gui.DrawContext;

public class CrosshairModule extends Module {
    public final Setting size = add(Setting.number("Size", 5, 1, 20));
    public final Setting gap = add(Setting.number("Gap", 2, 0, 12));
    public final Setting thickness = add(Setting.number("Thickness", 1, 1, 6));
    public final Setting dot = add(Setting.bool("Center Dot", false));
    public final Setting r = add(Setting.number("Red", 255, 0, 255));
    public final Setting g = add(Setting.number("Green", 255, 0, 255));
    public final Setting b = add(Setting.number("Blue", 255, 0, 255));

    public CrosshairModule() { super("Crosshair", "Custom crosshair replacing the vanilla one"); }

    /** Called from InGameHudMixin in place of the vanilla crosshair. */
    public void render(DrawContext ctx) {
        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        int sz = size.asInt(), gp = gap.asInt(), t = thickness.asInt(), o = t / 2;
        int color = 0xFF000000 | (r.asInt() << 16) | (g.asInt() << 8) | b.asInt();

        ctx.fill(cx - gp - sz, cy - o, cx - gp, cy - o + t, color);      // left
        ctx.fill(cx + gp, cy - o, cx + gp + sz, cy - o + t, color);      // right
        ctx.fill(cx - o, cy - gp - sz, cx - o + t, cy - gp, color);      // top
        ctx.fill(cx - o, cy + gp, cx - o + t, cy + gp + sz, color);      // bottom
        if (dot.asBool()) ctx.fill(cx - o, cy - o, cx - o + t, cy - o + t, color);
    }
}
