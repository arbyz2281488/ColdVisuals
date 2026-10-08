package com.example.visuals.mixin;

import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.ScoreboardHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void visuals$crosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!ModuleManager.CROSSHAIR.enabled) return;
        if (MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
            ModuleManager.CROSSHAIR.render(context);
        }
        ci.cancel();
    }

    /**
     * Scoreboard: hide the vanilla sidebar, our module draws a restyled one in its place.
     * The full descriptor is required: InGameHud has two methods with this name
     * (one takes a RenderTickCounter, one takes the objective).
     */
    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$scoreboard(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        ScoreboardHudModule m = ModuleManager.SCOREBOARD;
        if (!m.enabled) return;
        m.capture(objective);
        ci.cancel();
    }
}
