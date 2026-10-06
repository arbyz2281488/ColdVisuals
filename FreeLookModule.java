package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Hold the key (default: Left Alt, change it in the GUI) and move the mouse: the camera looks around,
 * your character keeps facing where it faced. Release the key to return (see EntityMixin and CameraMixin).
 */
public class FreeLookModule extends Module {
    public static boolean active;
    public static float yaw, pitch;
    private boolean was;

    public FreeLookModule() {
        super("FreeLook", "Hold the key to look around without turning");
        enableActionKey("Hold key");
        actionKey = GLFW.GLFW_KEY_LEFT_ALT;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.getWindow() == null) {
            active = false;
            was = false;
            return;
        }
        boolean hold = enabled && actionKey >= 0 && mc.currentScreen == null
                && InputUtil.isKeyPressed(mc.getWindow().getHandle(), actionKey);
        if (hold && !was) {
            yaw = mc.player.getYaw();
            pitch = mc.player.getPitch();
        }
        active = hold;
        was = hold;
    }
}
