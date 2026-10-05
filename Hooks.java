package com.example.visuals;

import com.example.visuals.modules.ChatTimeModule;
import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.NameMentionModule;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.Locale;

/** Event hooks: incoming chat (ChatTime, NameMention) and attacks (HitSounds, HitParticles). */
public class Hooks {
    public static void register() {
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> overlay ? message : onChat(message));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) onAttack(entity);
            return ActionResult.PASS;
        });
    }

    private static Text onChat(Text message) {
        MinecraftClient mc = MinecraftClient.getInstance();
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
            }
        }

        ChatTimeModule time = ModuleManager.CHATTIME;
        if (time.enabled) {
            result = Text.literal("§7[" + time.stamp() + "] §r").append(result);
        }
        return result;
    }

    private static void onAttack(Entity target) {
        ModuleManager.HITSOUNDS.play();
        ModuleManager.HITPARTICLES.spawn(target);
    }
}
