package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * One key moves the chosen item from your inventory into your offhand (same as moving it by hand).
 * Item: 1 = totem, 2 = golden apple, 3 = shield, 4 = ender pearl. Bind the key in the GUI.
 */
public class ItemSwapModule extends Module {
    public final Setting item = add(Setting.number("Item 1T 2G 3S 4P", 1, 1, 4));

    public ItemSwapModule() {
        super("ItemSwap", "Moves the chosen item into your offhand with one key");
        enableActionKey("Swap key");
    }

    @Override
    public void onActionKey(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;
        Item wanted = switch (item.asInt()) {
            case 2 -> Items.GOLDEN_APPLE;
            case 3 -> Items.SHIELD;
            case 4 -> Items.ENDER_PEARL;
            default -> Items.TOTEM_OF_UNDYING;
        };
        if (mc.player.getOffHandStack().isOf(wanted)) return;

        int src = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(wanted)) { src = i; break; }
        }
        if (src < 0) return;

        // player screen handler slot ids: hotbar 0..8 -> 36..44, main inventory 9..35 -> 9..35, offhand -> 45
        int srcId = src < 9 ? 36 + src : src;
        var handler = mc.player.playerScreenHandler;
        var im = mc.interactionManager;
        im.clickSlot(handler.syncId, srcId, 0, SlotActionType.PICKUP, mc.player);
        im.clickSlot(handler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
        im.clickSlot(handler.syncId, srcId, 0, SlotActionType.PICKUP, mc.player);
    }
}
