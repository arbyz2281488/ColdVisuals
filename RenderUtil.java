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

    /** Rounded rectangle with a vertical gradient from top to bottom colour (both ARGB). */
    public static void roundedGradient(DrawContext ctx, int x, int y, int w, int h, int radius, int top, int bottom) {
        int r = Math.min(radius, Math.min(w, h) / 2);
        for (int i = 0; i < h; i++) {
            int inset = 0;
            if (i < r) inset = corner(r, r - i);
            else if (i >= h - r) inset = corner(r, i - (h - r) + 1);
            float t = h <= 1 ? 0f : (float) i / (h - 1);
            ctx.fill(x + inset, y + i, x + w - inset, y + i + 1, mix(top, bottom, t));
        }
    }

    public static int mix(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (((c1 >>> 24) & 255) * (1 - t) + ((c2 >>> 24) & 255) * t);
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int corner(int r, int dy) {
        double dyc = dy - 0.5;
        double dx = Math.sqrt(Math.max(0, r * r - dyc * dyc));
        return (int) Math.round(r - dx);
    }
}
