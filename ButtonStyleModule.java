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
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Rounded gradient buttons with a glowing accent edge. Drawn on top of the normal buttons after the screen is
 * rendered, so it works without changing any game code and keeps whatever text the button has.
 */
public class ButtonStyleModule extends Module {
    public final Setting glow = add(Setting.bool("Hover Glow", true));

    private final Map<ClickableWidget, Float> hover = new WeakHashMap<>();

    public ButtonStyleModule() {
        super("ButtonStyle", "Rounded gradient buttons in menus");
        enabled = true; // on by default (a saved config can still turn it off)
    }

    private float hover(ClickableWidget w, boolean hovered) {
        float h = hover.getOrDefault(w, 0f);
        h += ((hovered ? 1f : 0f) - h) * 0.3f;
        hover.put(w, h);
        return h;
    }

    /** Called after a screen has been drawn: restyles its plain buttons. */
    public void draw(DrawContext ctx, Screen screen, int mx, int my) {
        if (!enabled) return;
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int accent = ModuleManager.THEME.accent();

        for (ClickableWidget w : Screens.getButtons(screen)) {
            if (!w.visible) continue;
            if (!(w instanceof ButtonWidget || w instanceof CyclingButtonWidget<?>)) continue;
            if (w instanceof TexturedButtonWidget || w instanceof PressableTextWidget || w instanceof TextIconButtonWidget) continue;
            if (w.getWidth() <= 24) continue; // small square buttons are icon buttons
            Text msg = w.getMessage();
            if (msg == null || msg.getString().isEmpty()) continue;

            int x = w.getX(), y = w.getY(), bw = w.getWidth(), bh = w.getHeight();
            boolean active = w.active;
            boolean hov = active && mx >= x && mx < x + bw && my >= y && my < y + bh;
            float k = active ? hover(w, hov) : 0f;

            // edge: dim ice-blue normally, accent colour when hovered
            int edgeTop = active ? RenderUtil.mix(0xFF4A7DB8, glow.asBool() ? accent : 0xFF7AB4F0, k) : 0xFF384254;
            int edgeBottom = active ? RenderUtil.mix(0xFF1E3A66, accent & 0xFFFFFF | 0xAA000000, k) : 0xFF283040;
            RenderUtil.roundedGradient(ctx, x - 1, y - 1, bw + 2, bh + 2, 6, edgeTop, edgeBottom);

            // body: lighter at the top, darker at the bottom
            int top = active ? RenderUtil.mix(0xFF1C3A66, 0xFF2E5A96, k) : 0xFF2A323E;
            int bottom = active ? RenderUtil.mix(0xFF0C1830, 0xFF183462, k) : 0xFF1C222C;
            RenderUtil.roundedGradient(ctx, x, y, bw, bh, 5, top, bottom);
            if (active) ctx.fill(x + 5, y + 1, x + bw - 5, y + 2, 0x30FFFFFF); // soft highlight line

            int color = active ? RenderUtil.mix(0xFFD6E6F8, 0xFFFFFFFF, k) : 0xFF7A8696;
            int tw = tr.getWidth(msg);
            int avail = bw - 8;
            float sc = tw > avail ? Math.max(0.6f, (float) avail / tw) : 1f;
            int dw = Math.round(tw * sc);

            ctx.enableScissor(x + 2, y, x + bw - 2, y + bh);
            MatrixStack m = ctx.getMatrices();
            m.push();
            m.translate(x + (bw - dw) / 2f, y + (bh - 8 * sc) / 2f, 0f);
            m.scale(sc, sc, 1f);
            ctx.drawTextWithShadow(tr, msg, 0, 0, color);
            m.pop();
            ctx.disableScissor();
        }
    }
}
