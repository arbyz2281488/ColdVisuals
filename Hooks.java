package com.example.visuals;

import com.example.visuals.gui.ClickGuiScreen;
import com.example.visuals.gui.MenuBackground;
import com.example.visuals.modules.ChatTimeModule;
import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.NameMentionModule;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.Locale;

/**
 * Event hooks: incoming chat (AntiSpam, NameMention, NameProtect, ChatTime)
 * and your attacks (FriendSave, HitSounds, HitParticles).
 */
public class Hooks {
    public static void register() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> overlay || ModuleManager.ANTISPAM.allow(message));
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> overlay ? message : onChat(message));

        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof TitleScreen && MenuBackground.firstTime(screen)) {
                ScreenEvents.afterRender(screen).register((s, ctx, mx, my, td) -> MenuBackground.drawBrand(ctx, s.width, s.height));
            }
            if (screen instanceof ClickGuiScreen || screen instanceof ChatScreen) return;
            if (!ModuleManager.SMOOTH.opened(screen)) return; // already hooked (screen was just resized)
            if (screen instanceof HandledScreen<?>) {
                ScreenEvents.afterRender(screen).register((s, ctx, mx, my, td) -> ModuleManager.ITEMCOLOR.render(s, ctx));
            }
            ScreenEvents.beforeRender(screen).register((s, ctx, mx, my, td) -> ModuleManager.SMOOTH.begin(ctx, s));
            ScreenEvents.afterRender(screen).register((s, ctx, mx, my, td) -> ModuleManager.SMOOTH.end(ctx));
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) {
                if (ModuleManager.FRIENDSAVE.enabled && entity instanceof PlayerEntity p
                        && FriendManager.isFriend(p.getName().getString())) {
                    return ActionResult.FAIL;
                }
                onAttack(entity);
            }
            return ActionResult.PASS;
        });
    }

    private static Text onChat(Text message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ModuleManager.SMOOTHCHAT.onMessage();
        Text result = message;

        NameMentionModule mention = ModuleManager.MENTION;
        if (mention.enabled && mc.player != null) {
            String plain = message.getString().toLowerCase(Locale.ROOT);
            String me = mc.player.getName().getString().toLowerCase(Locale.ROOT);
            String trimmed = plain.trim();
            boolean own = trimmed.startsWith(me) || trimmed.startsWith("<" + me + ">");
            if (!own && plain.contains(me)) {
                if (mention.highlight.asBool()) {
                    result = Text.literal("").append(result).formatted(Formatting.GOLD);
                }
                if (mention.sound.asBool()) {
                    mc.getSoundManager().play(
                            PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f));
                }
                ModuleManager.ISLAND.push("Тебя упомянули в чате");
            }
        }

        result = ModuleManager.NAMEPROTECT.apply(result);

        ChatTimeModule time = ModuleManager.CHATTIME;
        if (time.enabled) {
            result = Text.literal("§7[" + time.stamp() + "] §r").append(result);
        }
        return result;
    }

    private static void onAttack(Entity target) {
        ModuleManager.HITSOUNDS.play();
        ModuleManager.HITPARTICLES.spawn(target);
        ModuleManager.TARGETESP.setTarget(target);
        ModuleManager.HITBUBBLES.spawn(target);
    }
}
