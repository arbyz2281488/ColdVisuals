package com.example.visuals.gui;

import net.minecraft.client.gui.DrawContext;

public class RenderUtil {
    /** Filled rounded rectangle (color is ARGB). */
    public static void roundedRect(DrawContext ctx, int x, int y, int w, int h, int radius, int color) {
        int r = Math.min(radius, Math.min(w, h) / 2);
        for (int i = 0; i < h; i++) {
            int inset = 0;
            if (i < r) inset = corner(r, r - i);
            else if (i >= h - r) inset = corner(r, i - (h - r) + 1);
            ctx.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
        }
    }

    private static int corner(int r, int dy) {
        double dyc = dy - 0.5;
        double dx = Math.sqrt(Math.max(0, r * r - dyc * dyc));
        return (int) Math.round(r - dx);
    }
}
