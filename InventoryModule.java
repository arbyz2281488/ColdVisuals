package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class InventoryModule extends Module {
    public final Setting hotbar = add(Setting.bool("Show Hotbar", false));
    public final Setting title = add(Setting.bool("Show Title", true));
    public final Setting scale = add(Setting.number("Scale", 1.0, 0.5, 2.0));

    private static final int ACCENT = 0xFFB06CFF;

    public InventoryModule() {
        super("Inventory", "Shows your inventory on screen (draggable)");
        makeMovable(6, 160);
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        TextRenderer tr = mc.textRenderer;
        float s = (float) scale.value;
        int rows = hotbar.asBool() ? 4 : 3;
        int titleH = title.asBool() ? 12 : 0;
        int w = 9 * 18 + 8;
        int h = titleH + rows * 18 + 8;
        int sw = Math.round(w * s), sh = Math.round(h * s);
        int px = clampX(ctx, sw), py = clampY(ctx, sh);
        setBox(px, py, sw, sh);

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate((float) px, (float) py, 0f);
        m.scale(s, s, 1f);

        RenderUtil.roundedRect(ctx, 0, 0, w, h, 4, 0xB0101018);
        if (title.asBool()) ctx.drawTextWithShadow(tr, "Инвентарь", 6, 4, ACCENT);

        int ox = 4, oy = titleH + 4;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                // rows 0..2 = main inventory (slots 9..35), row 3 = hotbar (slots 0..8)
                int slot = row < 3 ? 9 + row * 9 + col : col;
                int sx = ox + col * 18, sy = oy + row * 18;
                ctx.fill(sx, sy, sx + 17, sy + 17, 0x22FFFFFF);
                ItemStack st = mc.player.getInventory().getStack(slot);
                if (st.isEmpty()) continue;
                ctx.drawItem(st, sx + 1, sy + 1);
                ctx.drawStackOverlay(tr, st, sx + 1, sy + 1);
            }
        }
        m.pop();
    }
}
