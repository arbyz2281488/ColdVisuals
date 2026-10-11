package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Ice-blue rounded buttons in menus. Drawn on top of the normal buttons after the screen is rendered,
 * so it works without changing any game code.
 */
public class ButtonStyleModule extends Module {
    public final Setting glow = add(Setting.bool("Hover Glow", true));

    private final Map<ClickableWidget, Float> hover = new WeakHashMap<>();

    public ButtonStyleModule() {
        super("ButtonStyle", "Rounded ice-blue buttons in menus");
        enabled = true; // on by default (a saved config can still turn it off)
    }

    private float hover(ClickableWidget w, boolean hovered) {
        float h = hover.getOrDefault(w, 0f);
        h += ((hovered ? 1f : 0f) - h) * 0.3f;
        hover.put(w, h);
        return h;
    }

    private static int lerp(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (((c1 >>> 24) & 255) * (1 - t) + ((c2 >>> 24) & 255) * t);
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Called after a screen has been drawn: restyles its plain buttons. */
    public void draw(DrawContext ctx, Screen screen, int mx, int my) {
        if (!enabled) return;
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        for (ClickableWidget w : Screens.getButtons(screen)) {
            if (!w.visible) continue;
            if (!(w instanceof ButtonWidget || w instanceof CyclingButtonWidget<?>)) continue;
            if (w instanceof TexturedButtonWidget || w instanceof PressableTextWidget) continue;
            Text msg = w.getMessage();
            if (msg == null || msg.getString().isEmpty()) continue;

            int x = w.getX(), y = w.getY(), bw = w.getWidth(), bh = w.getHeight();
            boolean active = w.active;
            boolean hov = active && mx >= x && mx < x + bw && my >= y && my < y + bh;
            float k = active ? hover(w, hov) : 0f;

            int border = active ? lerp(0xFF3C6EA8, glow.asBool() ? 0xFF6CC8FF : 0xFF5A9ADA, k) : 0xFF303A48;
            int fill = active ? lerp(0xFA102038, 0xFA1C3862, k) : 0xFA202834;
            RenderUtil.roundedRect(ctx, x - 1, y - 1, bw + 2, bh + 2, 6, border);
            RenderUtil.roundedRect(ctx, x, y, bw, bh, 5, fill);

            int color = active ? lerp(0xFFD0E2F6, 0xFFFFFFFF, k) : 0xFF7A8696;
            ctx.drawTextWithShadow(tr, msg, x + (bw - tr.getWidth(msg)) / 2, y + (bh - 8) / 2, color);
        }
    }
}
