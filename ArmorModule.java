package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArmorModule extends Module {
    public final Setting hands = add(Setting.bool("Show Hands", false));
    public final Setting percent = add(Setting.bool("Show Percent", true));
    public final Setting bars = add(Setting.bool("Show Bars", true));

    public ArmorModule() {
        super("Armor", "Shows worn armor and durability (draggable)");
        makeMovable(10000, 120); // huge X = right screen edge by default
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        List<ItemStack> items = new ArrayList<>();
        EquipmentSlot[] order = hands.asBool()
                ? new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}
                : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET};
        for (EquipmentSlot slot : order) {
            ItemStack st = mc.player.getEquippedStack(slot);
            if (!st.isEmpty()) items.add(st);
        }
        if (items.isEmpty()) return;

        TextRenderer tr = mc.textRenderer;
        int rowH = 20;
        int w = 26 + (percent.asBool() || bars.asBool() ? 36 : 0) + 6;
        int h = items.size() * rowH + 6;
        int px = clampX(ctx, w), py = clampY(ctx, h);
        setBox(px, py, w, h);

        RenderUtil.roundedRect(ctx, px, py, w, h, 4, 0xB0101018);
        int ty = py + 3;
        for (ItemStack st : items) {
            ctx.drawItem(st, px + 4, ty + 2);
            if (st.isDamageable() && st.getMaxDamage() > 0) {
                int max = st.getMaxDamage();
                int pct = Math.max(0, Math.round(100f * (max - st.getDamage()) / max));
                int color = pct > 50 ? 0xFF55FF55 : (pct > 25 ? 0xFFFFD84D : 0xFFFF5555);
                if (percent.asBool()) ctx.drawTextWithShadow(tr, pct + "%", px + 26, ty + 3, color);
                if (bars.asBool()) {
                    ctx.fill(px + 26, ty + 13, px + 26 + 30, ty + 15, 0xFF333333);
                    ctx.fill(px + 26, ty + 13, px + 26 + Math.round(30 * pct / 100f), ty + 15, color);
                }
            } else {
                ctx.drawStackOverlay(tr, st, px + 4, ty + 2);
            }
            ty += rowH;
        }
    }
}
