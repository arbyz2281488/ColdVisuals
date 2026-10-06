package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Locks hotbar slots against the drop key (Q) so you can't throw an item away by accident.
 * Switch the slots on in the GUI or press .lockslot while holding the slot you want to lock.
 * (Dragging items out of an open inventory is not blocked.)
 */
public class SlotLockModule extends Module {
    private final Setting[] slots = new Setting[9];

    public SlotLockModule() {
        super("SlotLock", "Q can't drop items from locked hotbar slots");
        for (int i = 0; i < 9; i++) slots[i] = add(Setting.bool("Slot " + (i + 1), false));
    }

    /** Called at the start of every tick, before the game reads the drop key. */
    public void beforeInput(MinecraftClient mc) {
        if (!enabled || mc.player == null) return;
        int i = mc.player.getInventory().selectedSlot;
        if (i < 0 || i > 8 || !slots[i].asBool()) return;
        while (mc.options.dropKey.wasPressed()) {
            // swallow the key press
        }
    }

    public void toggleCurrent(MinecraftClient mc) {
        if (mc.player == null) return;
        int i = mc.player.getInventory().selectedSlot;
        if (i < 0 || i > 8) return;
        slots[i].value = slots[i].asBool() ? 0 : 1;
        mc.player.sendMessage(Text.literal("§d[ColdVisuals] §fСлот " + (i + 1)
                + (slots[i].asBool() ? " заблокирован" : " разблокирован")), false);
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
        for (int i = 0; i < 9; i++) {
            if (!slots[i].asBool()) continue;
            int x = w / 2 - 88 + i * 20;
            ctx.fill(x, h - 26, x + 16, h - 24, 0xFFFF5555);
        }
    }
}
