package com.example.visuals.mixin;

import com.example.visuals.modules.CustomFogModule;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.entity.effect.StatusEffects;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    /** CustomFog: change fog distance and colour. Blindness and Darkness keep their own fog. */
    @Inject(method = "applyFog", at = @At("RETURN"), cancellable = true, require = 0)
    private static void visuals$customFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f color,
                                          float viewDistance, boolean thickenFog, float tickDelta,
                                          CallbackInfoReturnable<Fog> cir) {
        CustomFogModule m = ModuleManager.CUSTOMFOG;
        if (!m.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && (mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                || mc.player.hasStatusEffect(StatusEffects.DARKNESS))) {
            return;
        }

        Fog f = cir.getReturnValue();
        float start = f.start(), end = f.end();
        if (m.noFog.asBool()) {
            start = 1.0E7f;
            end = 1.0E7f + 100f;
        } else {
            float k = (float) m.distance.value;
            start *= k;
            end *= k;
        }
        float r = f.red(), g = f.green(), b = f.blue();
        if (m.recolor.asBool()) {
            r = m.r.asInt() / 255f;
            g = m.g.asInt() / 255f;
            b = m.b.asInt() / 255f;
        }
        cir.setReturnValue(new Fog(start, end, f.shape(), r, g, b, f.alpha()));
    }
}
