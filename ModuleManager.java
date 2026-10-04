package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class ModuleManager {
    public static final ZoomModule ZOOM = new ZoomModule();
    public static final FovModule FOV = new FovModule();
    public static final HudModule HUD = new HudModule();
    public static final CrosshairModule CROSSHAIR = new CrosshairModule();
    public static final WatermarkModule WATERMARK = new WatermarkModule();
    public static final CooldownsModule COOLDOWNS = new CooldownsModule();
    public static final InventoryModule INVENTORY = new InventoryModule();
    public static final ArmorModule ARMOR = new ArmorModule();
    public static final GpsModule GPS = new GpsModule();

    // To add a module: create a class extending Module and put it in this list.
    private static final List<Module> MODULES =
            List.of(WATERMARK, GPS, COOLDOWNS, INVENTORY, ARMOR, HUD, ZOOM, FOV, CROSSHAIR);

    public static List<Module> all() { return MODULES; }

    public static Module byName(String name) {
        for (Module m : MODULES) if (m.name.equalsIgnoreCase(name)) return m;
        return null;
    }

    public static void tick(MinecraftClient mc) {
        for (Module m : MODULES) m.onTick(mc);
    }

    public static void hud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        for (Module m : MODULES) {
            m.boxW = 0;
            if (m.enabled) m.onHud(ctx, mc);
        }
    }
}
