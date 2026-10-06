package com.example.visuals.mixin;

import com.example.visuals.gui.Project;
import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.ScreenStretchModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
        Project.fov = fov;
        cir.setReturnValue(fov);
    }

    /** Removals: no camera shake when you take damage. */
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$noHurtCam(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (ModuleManager.REMOVALS.enabled && ModuleManager.REMOVALS.hurtCam.asBool()) ci.cancel();
    }

    /** ScreenStretch: squeeze or widen the picture horizontally. */
    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), require = 0)
    private void visuals$stretch(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        ScreenStretchModule s = ModuleManager.STRETCH;
        if (s.enabled && Math.abs(s.stretch.value - 1.0) > 0.001) {
            cir.getReturnValue().scale((float) s.stretch.value, 1f, 1f);
        }
    }
}
