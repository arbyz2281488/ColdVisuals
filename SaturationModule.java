package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.Locale;

public class SaturationModule extends Module {
    public SaturationModule() {
        super("Saturation", "Shows food level and saturation (draggable)");
        makeMovable(6, 270);
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        var hunger = mc.player.getHungerManager();
        String text = String.format(Locale.ROOT, "Еда %d  |  Насыщ. %.1f",
                hunger.getFoodLevel(), hunger.getSaturationLevel());
        int w = mc.textRenderer.getWidth(text) + 12, h = 16;
        int px = clampX(ctx, w), py = clampY(ctx, h);
        setBox(px, py, w, h);
        RenderUtil.roundedRect(ctx, px, py, w, h, 8, 0xB0101018);
        ctx.drawTextWithShadow(mc.textRenderer, text, px + 6, py + 4, 0xFFFFFFFF);
    }
}
