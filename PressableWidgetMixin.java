package com.example.visuals.mixin;

import net.minecraft.client.gui.widget.PressableWidget;
import org.spongepowered.asm.mixin.Mixin;

/** Intentionally empty: button styling is drawn by ButtonStyleModule after the screen (no game code is changed). */
@Mixin(PressableWidget.class)
public class PressableWidgetMixin {
}
