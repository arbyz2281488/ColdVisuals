package com.example.visuals;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Recolours a PNG into a colour family. Presets: 0 original, 1 ice, 2 fire, 3 toxic, 4 violet, 5 gold, 6 dark. */
public final class Recolor {
    private static final float[] HUE = {0f, 200f / 360f, 12f / 360f, 115f / 360f, 275f / 360f, 45f / 360f, 0f};
    private static final float[] SAT = {0f, 0.55f, 0.75f, 0.65f, 0.60f, 0.75f, 0f};
    public static final int COUNT = HUE.length;

    private Recolor() {}

    public static byte[] apply(byte[] png, int preset) throws IOException {
        if (preset <= 0 || preset >= COUNT) return png;
        BufferedImage src = ImageIO.read(new ByteArrayInputStream(png));
        if (src == null) return png;
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        float[] hsb = new float[3];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = src.getRGB(x, y);
                int a = p >>> 24;
                if (a == 0) {
                    out.setRGB(x, y, p);
                    continue;
                }
                Color.RGBtoHSB((p >> 16) & 255, (p >> 8) & 255, p & 255, hsb);
                float hue = hsb[0], sat = hsb[1], val = hsb[2];
                if (preset == 6) {
                    sat *= 0.25f;
                    val *= 0.6f;
                } else if (sat < 0.12f) {
                    hue = HUE[preset];
                    sat = SAT[preset] * 0.8f;
                } else {
                    hue = HUE[preset] + 0.12f * (hue - 0.5f);
                    sat = Math.max(sat, SAT[preset] * 0.8f);
                }
                hue -= (float) Math.floor(hue);
                int rgb = Color.HSBtoRGB(hue, Math.min(1f, sat), val);
                out.setRGB(x, y, (a << 24) | (rgb & 0xFFFFFF));
            }
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ImageIO.write(out, "png", bo);
        return bo.toByteArray();
    }
}
