package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class ModuleManager {
    public static final ZoomModule ZOOM = new ZoomModule();
    public static final FovModule FOV = new FovModule();
    public static final HudModule HUD = new HudModule();
    public static final CrosshairModule CROSSHAIR = new CrosshairModule();

    // To add a module: create a class extending Module and put it in this list.
    private static final List<Module> MODULES = List.of(ZOOM, FOV, HUD, CROSSHAIR);

    public static List<Module> all() { return MODULES; }

    public static void tick(MinecraftClient mc) {
        for (Module m : MODULES) m.onTick(mc);
    }

    public static void hud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        for (Module m : MODULES) if (m.enabled) m.onHud(ctx, mc);
    }
}
