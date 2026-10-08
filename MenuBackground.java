package com.example.visuals.gui;

import com.example.visuals.modules.MenuStyleModule;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

/** Ice-blue main menu: your logo as the background, falling snow, and Cold branding. */
public class MenuBackground {
    private static final Identifier ID = Identifier.of("visuals", "menu_bg");
    private static boolean tried, ok;
    private static int texW, texH;
    private static final Map<Screen, Boolean> BRANDED = new WeakHashMap<>();

    private static final int FLAKES = 90;
    private static final float[] FX = new float[FLAKES], FY = new float[FLAKES], SPEED = new float[FLAKES],
            PHASE = new float[FLAKES];
    private static final int[] SIZE = new int[FLAKES], ALPHA = new int[FLAKES];

    static {
        Random r = new Random(7);
        for (int i = 0; i < FLAKES; i++) {
            FX[i] = r.nextFloat();
            FY[i] = r.nextFloat();
            SPEED[i] = 0.5f + r.nextFloat() * 1.2f;
            PHASE[i] = r.nextFloat() * 6.28f;
            SIZE[i] = r.nextInt(4) == 0 ? 2 : 1;
            ALPHA[i] = 90 + r.nextInt(130);
        }
    }

    private static void ensureTexture() {
        if (tried) return;
        tried = true;
        try {
            byte[] bytes = Base64.getDecoder().decode(String.join("", MenuImageData.CHUNKS));
            NativeImage img = NativeImage.read(new ByteArrayInputStream(bytes));
            texW = img.getWidth();
            texH = img.getHeight();
            NativeImageBackedTexture tex = new NativeImageBackedTexture(img);
            tex.setFilter(true, false);
            MinecraftClient.getInstance().getTextureManager().registerTexture(ID, tex);
            ok = true;
        } catch (Throwable t) {
            ok = false; // fall back to a plain navy background
        }
    }

    /** True the first time this screen is seen (so branding is hooked only once). */
    public static boolean firstTime(Screen s) {
        return BRANDED.putIfAbsent(s, Boolean.TRUE) == null;
    }

    public static void drawBackground(DrawContext ctx, int w, int h) {
        MenuStyleModule m = ModuleManager.MENUSTYLE;
        ensureTexture();
        long now = System.currentTimeMillis();
        double t = now / 1000.0;
        int navyRgb = MenuImageData.NAVY & 0xFFFFFF;

        ctx.fill(0, 0, w, h, MenuImageData.NAVY);

        if (ok) {
            float breathe = 1f + 0.012f * (float) Math.sin(t * 0.6);
            float scale = h * 0.95f / texH * breathe;
            int dw = Math.round(texW * scale), dh = Math.round(texH * scale);
            int x = (w - dw) / 2;
            int y = (h - dh) / 2 + (int) (Math.sin(t * 0.5) * 4);
            ctx.drawTexture(RenderLayer::getGuiTextured, ID, x, y, 0f, 0f, dw, dh, texW, texH, texW, texH);

            int dim = (int) ((100 - m.logo.value) / 100.0 * 255);
            if (dim > 0) ctx.fill(0, 0, w, h, (dim << 24) | navyRgb);
        }

        int dark = (int) (m.darken.value / 100.0 * 200);
        if (dark > 0) ctx.fillGradient(0, 0, w, h, (dark / 2 << 24) | 0x00040C, (dark << 24) | 0x00040C);

        if (m.snow.asBool()) {
            for (int i = 0; i < FLAKES; i++) {
                float yy = ((FY[i] * h + (float) t * SPEED[i] * h * 0.06f) % (h + 8)) - 4;
                float xx = ((FX[i] * w + (float) Math.sin(t * 0.8 + PHASE[i]) * 10f) % w + w) % w;
                int px = (int) xx, py = (int) yy, s = SIZE[i];
                ctx.fill(px, py, px + s, py + s, (ALPHA[i] << 24) | 0xDCEBFF);
            }
        }

        // soft vignette
        int edge = h / 5;
        ctx.fillGradient(0, 0, w, edge, 0x66000000, 0x00000000);
        ctx.fillGradient(0, h - edge, w, h, 0x00000000, 0x77000000);
    }

    /** Cold branding in the corners of the title screen. */
    public static void drawBrand(DrawContext ctx, int w, int h) {
        if (!ModuleManager.MENUSTYLE.enabled) return;
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        // top-left logo
        RenderUtil.roundedRect(ctx, 8, 8, 112, 50, 8, 0x80101828);
        RenderUtil.roundedRect(ctx, 8, 8, 3, 50, 1, 0xFF6CC8FF);
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(18f, 13f, 0f);
        m.scale(3f, 3f, 1f);
        ctx.drawTextWithShadow(tr, "Cold", 0, 0, 0xFF9AD8FF);
        m.pop();
        ctx.drawTextWithShadow(tr, "visuals beta", 19, 42, 0xFF8FA9CC);

        // top-right tag
        String tag = "Cold  |  Right Shift - меню";
        int tw = tr.getWidth(tag) + 14;
        RenderUtil.roundedRect(ctx, w - tw - 8, 8, tw, 16, 8, 0x80101828);
        ctx.drawTextWithShadow(tr, tag, w - tw - 1, 12, 0xFFB9D6F2);
    }
}
