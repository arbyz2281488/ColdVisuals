package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/** Plays a sound when you attack something (see Hooks). Sound: 1 = pling, 2 = arrow hit, 3 = level up. */
public class HitSoundsModule extends Module {
    public final Setting sound = add(Setting.number("Sound", 1, 1, 3));
    public final Setting volume = add(Setting.number("Volume", 1.0, 0.1, 2.0));

    public HitSoundsModule() { super("HitSounds", "Plays a sound when you hit something"); }

    public void play() {
        if (!enabled) return;
        SoundEvent ev = switch (sound.asInt()) {
            case 2 -> SoundEvents.ENTITY_ARROW_HIT_PLAYER;
            case 3 -> SoundEvents.ENTITY_PLAYER_LEVELUP;
            default -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
        };
        MinecraftClient.getInstance().getSoundManager()
                .play(PositionedSoundInstance.master(ev, 1.0f, (float) volume.value));
    }
}
