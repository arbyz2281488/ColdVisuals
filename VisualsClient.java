package com.example.visuals;

import com.example.visuals.gui.ClickGuiScreen;
import com.example.visuals.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class VisualsClient implements ClientModInitializer {
    public static KeyBinding openGuiKey;
    public static KeyBinding zoomKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visuals.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.visuals"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visuals.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.visuals"));

        ClientTickEvents.START_CLIENT_TICK.register(client -> ModuleManager.SLOTLOCK.beforeInput(client));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            WindowIcon.apply(client);
            while (openGuiKey.wasPressed()) {
                client.setScreen(new ClickGuiScreen());
            }
            ModuleManager.tick(client);
            BindManager.tick(client);
            CommandBinds.tick(client);
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> ModuleManager.hud(ctx));

        Hooks.register();

        // .config / .bind commands are handled locally and never sent to the server
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> !CommandHandler.handle(message));
    }
}
