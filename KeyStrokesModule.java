package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class KeyStrokesModule extends Module {
    public final Setting mouse = add(Setting.bool("Show Mouse", true));
    public final Setting space = add(Setting.bool("Show Space", true));

    public KeyStrokesModule() {
        super("KeyStrokes", "WASD, mouse buttons and space (draggable)");
        makeMovable(6, 300);
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        TextRenderer tr = mc.textRenderer;
        int k = 20, g = 2;
        int w = 3 * k + 2 * g;
        int h = k + g + k + (mouse.asBool() ? g + k : 0) + (space.asBool() ? g + 12 : 0);
        int px = clampX(ctx, w), py = clampY(ctx, h);
        setBox(px, py, w, h);

        key(ctx, tr, px + k + g, py, k, k, "W", mc.options.forwardKey.isPressed());
        int y2 = py + k + g;
        key(ctx, tr, px, y2, k, k, "A", mc.options.leftKey.isPressed());
        key(ctx, tr, px + k + g, y2, k, k, "S", mc.options.backKey.isPressed());
        key(ctx, tr, px + 2 * (k + g), y2, k, k, "D", mc.options.rightKey.isPressed());

        int y3 = y2 + k + g;
        if (mouse.asBool()) {
            int bw = (w - g) / 2;
            key(ctx, tr, px, y3, bw, k, "LMB", mc.options.attackKey.isPressed());
            key(ctx, tr, px + bw + g, y3, w - bw - g, k, "RMB", mc.options.useKey.isPressed());
            y3 += k + g;
        }
        if (space.asBool()) key(ctx, tr, px, y3, w, 12, "SPACE", mc.options.jumpKey.isPressed());
    }

    private static void key(DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, String label, boolean pressed) {
        RenderUtil.roundedRect(ctx, x, y, w, h, 3, pressed ? 0xC0B06CFF : 0xB0101018);
        ctx.drawTextWithShadow(tr, label, x + (w - tr.getWidth(label)) / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }
}
