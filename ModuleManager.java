package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;

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
    public static final MiddleClickPearlModule PEARL = new MiddleClickPearlModule();
    public static final ElytraSwapModule ELYTRASWAP = new ElytraSwapModule();
    public static final ColdTagModule COLDTAG = new ColdTagModule();
    public static final ScreenTintModule SCREENTINT = new ScreenTintModule();
    public static final HurtFlashModule HURTFLASH = new HurtFlashModule();
    public static final IslandModule ISLAND = new IslandModule();
    public static final TntTimerModule TNTTIMER = new TntTimerModule();
    public static final TimeChangerModule TIMECHANGER = new TimeChangerModule();
    public static final HitboxesModule HITBOXES = new HitboxesModule();
    public static final SlotLockModule SLOTLOCK = new SlotLockModule();
    public static final FriendSaveModule FRIENDSAVE = new FriendSaveModule();
    public static final AntiSpamModule ANTISPAM = new AntiSpamModule();
    public static final NameProtectModule NAMEPROTECT = new NameProtectModule();
    public static final AutoCommandModule AUTOCOMMAND = new AutoCommandModule();
    public static final GammaModule GAMMA = new GammaModule();
    public static final TargetEspModule TARGETESP = new TargetEspModule();
    public static final ItemSwapModule ITEMSWAP = new ItemSwapModule();
    public static final NoJumpDelayModule NOJUMPDELAY = new NoJumpDelayModule();
    public static final RemovalsModule REMOVALS = new RemovalsModule();
    public static final NoFluidModule NOFLUID = new NoFluidModule();
    public static final BlockViewModule BLOCKVIEW = new BlockViewModule();
    public static final ScreenStretchModule STRETCH = new ScreenStretchModule();
    public static final ViewModelModule VIEWMODEL = new ViewModelModule();
    public static final SeeOwnNameModule SEEOWNNAME = new SeeOwnNameModule();
    public static final DamageNumbersModule DAMAGENUMBERS = new DamageNumbersModule();
    public static final KillEffectsModule KILLEFFECTS = new KillEffectsModule();
    public static final SmoothScreensModule SMOOTH = new SmoothScreensModule();
    public static final FullTabListModule FULLTAB = new FullTabListModule();
    public static final FreeLookModule FREELOOK = new FreeLookModule();
    public static final ChinaHatModule CHINAHAT = new ChinaHatModule();
    public static final ItemColorModule ITEMCOLOR = new ItemColorModule();

    // To add a module: create a class extending Module and put it in this list.
    private static final List<Module> MODULES = List.of(
            SCREENTINT, HURTFLASH, WATERMARK, ISLAND, GPS, COOLDOWNS, INVENTORY, ARMOR, HUD, TOTEMS, KEYSTROKES, KEYBINDS, TARGET,
            FRIENDS, SATURATION, CHATTIME, MENTION, DEATHCORDS, HITSOUNDS, HITPARTICLES, TRAILS,
            JUMPCIRCLE, WORLDPARTICLES, PEARLPARTICLES, AUTOSPRINT, PEARL, ELYTRASWAP, COLDTAG,
            TNTTIMER, TIMECHANGER, HITBOXES, SLOTLOCK, FRIENDSAVE, ANTISPAM, NAMEPROTECT, AUTOCOMMAND,
            GAMMA, TARGETESP, ITEMSWAP, NOJUMPDELAY,
            REMOVALS, NOFLUID, BLOCKVIEW, STRETCH, VIEWMODEL, SEEOWNNAME, DAMAGENUMBERS, KILLEFFECTS, SMOOTH,
            FULLTAB, FREELOOK, CHINAHAT, ITEMCOLOR,
            ZOOM, FOV, CROSSHAIR);

    private static void cat(String category, Module... mods) {
        for (Module m : mods) m.category = category;
    }

    static {
        cat("Interface", WATERMARK, ISLAND, GPS, COOLDOWNS, INVENTORY, ARMOR, HUD, TOTEMS, KEYSTROKES,
                KEYBINDS, TARGET, FRIENDS, SATURATION, TNTTIMER, SMOOTH, FULLTAB, ITEMCOLOR);
        cat("Helper", AUTOSPRINT, PEARL, ELYTRASWAP, ITEMSWAP, NOJUMPDELAY, AUTOCOMMAND, SLOTLOCK,
                FRIENDSAVE, ANTISPAM);
        cat("Visuals", SCREENTINT, HURTFLASH, TRAILS, JUMPCIRCLE, WORLDPARTICLES, PEARLPARTICLES,
                HITPARTICLES, TARGETESP, GAMMA, TIMECHANGER, HITBOXES, ZOOM, FOV, CROSSHAIR,
                REMOVALS, NOFLUID, BLOCKVIEW, STRETCH, VIEWMODEL, SEEOWNNAME, DAMAGENUMBERS, KILLEFFECTS, FREELOOK, CHINAHAT);
        cat("Other", COLDTAG, MENTION, DEATHCORDS, HITSOUNDS, NAMEPROTECT, CHATTIME);
    }

    public static List<Module> all() { return MODULES; }

    public static Module byName(String name) {
        for (Module m : MODULES) if (m.name.equalsIgnoreCase(name)) return m;
        return null;
    }

    public static void tick(MinecraftClient mc) {
        for (Module m : MODULES) m.onTick(mc);

        if (mc.getWindow() == null) return;
        long handle = mc.getWindow().getHandle();
        for (Module m : MODULES) {
            boolean ready = m.enabled && mc.currentScreen == null && mc.player != null;
            if (m.hasActionKey && m.actionKey >= 0) {
                boolean down = InputUtil.isKeyPressed(handle, m.actionKey);
                if (down && !m.actionWasDown && ready) m.onActionKey(mc);
                m.actionWasDown = down;
            } else {
                m.actionWasDown = false;
            }
            if (m.hasActionKey2 && m.actionKey2 >= 0) {
                boolean down2 = InputUtil.isKeyPressed(handle, m.actionKey2);
                if (down2 && !m.action2WasDown && ready) m.onActionKey2(mc);
                m.action2WasDown = down2;
            } else {
                m.action2WasDown = false;
            }
        }
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
