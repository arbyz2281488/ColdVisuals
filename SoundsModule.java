package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

/** Soft click sounds in the Cold GUI. */
public class SoundsModule extends Module {
    public final Setting volume = add(Setting.number("Volume", 0.5, 0.1, 1.0));

    public SoundsModule() { super("Sounds", "Click sounds in the GUI"); }

    public void click() {
        if (!enabled) return;
        MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.8f, (float) volume.value));
    }
}
