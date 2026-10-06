package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Red flash over the screen when you take damage. */
public class HurtFlashModule extends Module {
    public final Setting strength = add(Setting.number("Strength %", 40, 5, 100));

    public HurtFlashModule() { super("HurtFlash", "Red screen flash when you get hurt"); }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        int ht = mc.player.hurtTime;
        if (ht <= 0) return;
        float k = Math.min(1f, ht / 10f);
        int a = (int) (strength.value / 100.0 * 160 * k);
        ctx.fill(0, 0, ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight(), (a << 24) | 0xCC1010);
    }
}
