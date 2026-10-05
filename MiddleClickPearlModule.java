package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

/**
 * Uses an ender pearl or a firework rocket from your hotbar / offhand, then your selected slot returns
 * to what it was. Triggers (all optional):
 *  - Pearl key and Firework key: bind them in the GUI (right-click the module).
 *  - Middle mouse button: "Mode 1P 2F 3Auto" decides what it uses (1 = pearl, 2 = firework,
 *    3 = firework while gliding, otherwise pearl).
 * Commands: .pearl and .firework (can be put on a key with .key add).
 * Note: a firework gives a boost only while gliding (vanilla rule); on the ground it is placed on the block
 * you are looking at.
 */
public class MiddleClickPearlModule extends Module {
    public final Setting middle = add(Setting.bool("Middle Click", true));
    public final Setting mode = add(Setting.number("Mode 1P 2F 3Auto", 3, 1, 3));
    private boolean wasDown;

    public MiddleClickPearlModule() {
        super("MiddleClickPearl", "Pearl / firework from the hotbar (middle click and/or your bound keys)");
        enableActionKey("Pearl key");
        enableActionKey2("Firework key");
    }

    @Override
    public void onActionKey(MinecraftClient mc) { use(mc, Items.ENDER_PEARL); }

    @Override
    public void onActionKey2(MinecraftClient mc) { use(mc, Items.FIREWORK_ROCKET); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.getWindow() == null) return;
        boolean down = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;
        if (enabled && middle.asBool() && down && !wasDown && mc.currentScreen == null) {
            use(mc, byMode(mc));
        }
        wasDown = down;
    }

    private Item byMode(MinecraftClient mc) {
        return switch (mode.asInt()) {
            case 1 -> Items.ENDER_PEARL;
            case 2 -> Items.FIREWORK_ROCKET;
            default -> mc.player.isGliding() ? Items.FIREWORK_ROCKET : Items.ENDER_PEARL;
        };
    }

    private static boolean cooling(MinecraftClient mc, ItemStack s) {
        return mc.player.getItemCooldownManager().getCooldownProgress(s, 0f) > 0f;
    }

    private static void interact(MinecraftClient mc, Hand hand, boolean tryBlock) {
        if (tryBlock && mc.crosshairTarget instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            mc.interactionManager.interactBlock(mc.player, hand, bhr);
        } else {
            mc.interactionManager.interactItem(mc.player, hand);
        }
        mc.player.swingHand(hand);
    }

    public void use(MinecraftClient mc, Item wanted) {
        if (mc.player == null || mc.interactionManager == null) return;
        // fireworks on the ground need a block to be placed on; while gliding they just boost
        boolean tryBlock = wanted == Items.FIREWORK_ROCKET && !mc.player.isGliding();

        ItemStack off = mc.player.getOffHandStack();
        if (off.isOf(wanted)) {
            if (cooling(mc, off)) return;
            interact(mc, Hand.OFF_HAND, tryBlock);
            return;
        }

        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(wanted)) { slot = i; break; }
        }
        if (slot < 0) return;
        if (cooling(mc, mc.player.getInventory().getStack(slot))) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        interact(mc, Hand.MAIN_HAND, tryBlock);
        mc.player.getInventory().selectedSlot = prev;
    }
}
