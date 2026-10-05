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
    public static final TotemsModule TOTEMS = new TotemsModule();
    public static final KeyStrokesModule KEYSTROKES = new KeyStrokesModule();
    public static final KeyBindsHudModule KEYBINDS = new KeyBindsHudModule();
    public static final TargetHudModule TARGET = new TargetHudModule();
    public static final FriendsModule FRIENDS = new FriendsModule();
    public static final SaturationModule SATURATION = new SaturationModule();
    public static final ChatTimeModule CHATTIME = new ChatTimeModule();
    public static final NameMentionModule MENTION = new NameMentionModule();
    public static final DeathCordsModule DEATHCORDS = new DeathCordsModule();
    public static final HitSoundsModule HITSOUNDS = new HitSoundsModule();
    public static final HitParticlesModule HITPARTICLES = new HitParticlesModule();
    public static final TrailsModule TRAILS = new TrailsModule();
    public static final JumpCircleModule JUMPCIRCLE = new JumpCircleModule();
    public static final WorldParticlesModule WORLDPARTICLES = new WorldParticlesModule();
    public static final PearlParticlesModule PEARLPARTICLES = new PearlParticlesModule();
    public static final AutoSprintModule AUTOSPRINT = new AutoSprintModule();

    // To add a module: create a class extending Module and put it in this list.
    private static final List<Module> MODULES = List.of(
            WATERMARK, GPS, COOLDOWNS, INVENTORY, ARMOR, HUD, TOTEMS, KEYSTROKES, KEYBINDS, TARGET,
            FRIENDS, SATURATION, CHATTIME, MENTION, DEATHCORDS, HITSOUNDS, HITPARTICLES, TRAILS,
            JUMPCIRCLE, WORLDPARTICLES, PEARLPARTICLES, AUTOSPRINT, ZOOM, FOV, CROSSHAIR);

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
