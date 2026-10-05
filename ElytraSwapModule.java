package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Swaps your chestplate and elytra in one press (like moving them in the inventory by hand).
 * Enable it in the GUI, then right-click the module -> "Key" -> press the key you want.
 * Also available as the .elytraswap command.
 */
public class ElytraSwapModule extends Module {
    public ElytraSwapModule() {
        super("ElytraSwap", "Swap chestplate <-> elytra with your bound key (RMB: set key)");
        enableActionKey();
    }

    @Override
    public void onActionKey(MinecraftClient mc) { run(mc); }

    private static boolean isChestplate(ItemStack s) {
        return Registries.ITEM.getId(s.getItem()).getPath().endsWith("_chestplate");
    }

    public void run(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;

        boolean wearingElytra = mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
        int src = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            if (wearingElytra ? isChestplate(s) : s.isOf(Items.ELYTRA)) {
                src = i;
                break;
            }
        }
        if (src < 0) return;

        // player screen handler slot ids: hotbar 0..8 -> 36..44, main inventory 9..35 -> 9..35, chest armor -> 6
        int srcId = src < 9 ? 36 + src : src;
        int armorId = 6;
        var handler = mc.player.playerScreenHandler;
        var im = mc.interactionManager;
        im.clickSlot(handler.syncId, srcId, 0, SlotActionType.PICKUP, mc.player);
        im.clickSlot(handler.syncId, armorId, 0, SlotActionType.PICKUP, mc.player);
        im.clickSlot(handler.syncId, srcId, 0, SlotActionType.PICKUP, mc.player);
    }
}
