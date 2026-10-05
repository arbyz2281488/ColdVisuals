package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class TotemsModule extends Module {
    public final Setting hideZero = add(Setting.bool("Hide When Zero", false));
    private ItemStack icon;

    public TotemsModule() {
        super("Totems", "Counts totems of undying in your inventory (draggable)");
        makeMovable(6, 240);
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        if (icon == null) icon = new ItemStack(Items.TOTEM_OF_UNDYING);
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.TOTEM_OF_UNDYING)) count += s.getCount();
        }
        ItemStack off = mc.player.getOffHandStack();
        if (off.isOf(Items.TOTEM_OF_UNDYING)) count += off.getCount();
        if (count == 0 && hideZero.asBool()) return;

        String text = "x" + count;
        int w = 30 + mc.textRenderer.getWidth(text), h = 22;
        int px = clampX(ctx, w), py = clampY(ctx, h);
        setBox(px, py, w, h);
        RenderUtil.roundedRect(ctx, px, py, w, h, 4, 0xB0101018);
        ctx.drawItem(icon, px + 4, py + 3);
        ctx.drawTextWithShadow(mc.textRenderer, text, px + 24, py + 7, count > 0 ? 0xFFFFFFFF : 0xFFFF5555);
    }
}
