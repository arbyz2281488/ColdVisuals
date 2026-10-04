package com.example.visuals.mixin;

import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void visuals$modifyFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (!changingFov) return; // leave the held-item FOV alone
        float fov = cir.getReturnValueF();
        if (ModuleManager.FOV.enabled) fov += (float) ModuleManager.FOV.bonus.value;
        float zoom = ModuleManager.ZOOM.factor();
        if (zoom > 1.001f) fov /= zoom;
        cir.setReturnValue(fov);
    }
}
