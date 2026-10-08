package com.example.visuals.mixin;

import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.SkyColorModule;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    /** SkyColor: tint the sky. */
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void visuals$skyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        SkyColorModule m = ModuleManager.SKYCOLOR;
        if (!m.enabled) return;
        int orig = cir.getReturnValue();
        float k = (float) (m.blend.value / 100.0);
        int r = Math.round(((orig >> 16) & 255) * (1f - k) + m.r.asInt() * k);
        int g = Math.round(((orig >> 8) & 255) * (1f - k) + m.g.asInt() * k);
        int b = Math.round((orig & 255) * (1f - k) + m.b.asInt() * k);
        cir.setReturnValue(0xFF000000 | (r << 16) | (g << 8) | b);
    }
}
