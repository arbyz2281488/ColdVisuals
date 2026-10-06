package com.example.visuals.modules;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Map;
import java.util.WeakHashMap;

/** Smooth zoom-in animation when menus and containers open (inventory, chests, pause menu, ...). */
public class SmoothScreensModule extends Module {
    public final Setting duration = add(Setting.number("Duration ms", 200, 100, 500));
    public final Setting startScale = add(Setting.number("Start Scale", 0.9, 0.7, 1.0));

    private final Map<Screen, Long> opened = new WeakHashMap<>();
    private boolean pushed;

    public SmoothScreensModule() { super("SmoothScreens", "Smooth open animation for menus and containers"); }

    /** Returns true the first time this screen is seen. */
    public boolean opened(Screen s) {
        return opened.putIfAbsent(s, System.currentTimeMillis()) == null;
    }

    public void begin(DrawContext ctx, Screen s) {
        pushed = false;
        if (!enabled) return;
        Long t0 = opened.get(s);
        if (t0 == null) return;
        float t = Math.min(1f, (System.currentTimeMillis() - t0) / (float) duration.value);
        if (t >= 1f) return;
        float ease = 1f - (1f - t) * (1f - t) * (1f - t);
        float sc = (float) (startScale.value + (1.0 - startScale.value) * ease);
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(s.width / 2f * (1f - sc), s.height / 2f * (1f - sc), 0f);
        m.scale(sc, sc, 1f);
        pushed = true;
    }

    public void end(DrawContext ctx) {
        if (pushed) {
            ctx.getMatrices().pop();
            pushed = false;
        }
    }
}
