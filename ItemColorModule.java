package com.example.visuals.modules;

import com.example.visuals.mixin.HandledScreenAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;

/** Colours the slots of important items (totems, golden apples, pearls, elytra, netherite, ...) in inventories. */
public class ItemColorModule extends Module {
    public final Setting strength = add(Setting.number("Strength %", 40, 10, 100));

    public ItemColorModule() { super("ItemColor", "Highlights important items in inventories and chests"); }

    private static int rgbFor(ItemStack st) {
        String id = Registries.ITEM.getId(st.getItem()).getPath();
        if (id.equals("totem_of_undying")) return 0xFFD84D;
        if (id.contains("golden_apple")) return 0xFFAA00;
        if (id.equals("ender_pearl")) return 0x00C8C8;
        if (id.equals("elytra")) return 0x7CC8FF;
        if (id.startsWith("netherite_")) return 0xA06CFF;
        if (id.equals("firework_rocket")) return 0xFF5555;
        if (id.endsWith("shulker_box")) return 0xFF9AE0;
        if (id.equals("end_crystal") || id.equals("tnt")) return 0xFF3A3A;
        return 0;
    }

    /** Called after an inventory-like screen has been drawn. */
    public void render(Screen screen, DrawContext ctx) {
        if (!enabled || !(screen instanceof HandledScreen<?> hs)) return;
        HandledScreenAccessor acc = (HandledScreenAccessor) hs;
        int ox = acc.visuals$getX(), oy = acc.visuals$getY();
        int a = (int) (strength.value / 100.0 * 160);
        for (Slot slot : hs.getScreenHandler().slots) {
            ItemStack st = slot.getStack();
            if (st.isEmpty()) continue;
            int rgb = rgbFor(st);
            if (rgb == 0) continue;
            ctx.fill(ox + slot.x, oy + slot.y, ox + slot.x + 16, oy + slot.y + 16, (a << 24) | rgb);
        }
    }
}
