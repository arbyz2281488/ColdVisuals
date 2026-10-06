package com.example.visuals.mixin;

import com.example.visuals.modules.FreeLookModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    /** FreeLook: while the key is held, the mouse turns the camera instead of your character. */
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$freeLook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (!FreeLookModule.active) return;
        if ((Object) this != MinecraftClient.getInstance().player) return;
        FreeLookModule.yaw += (float) cursorDeltaX * 0.15f;
        FreeLookModule.pitch = MathHelper.clamp(FreeLookModule.pitch + (float) cursorDeltaY * 0.15f, -90f, 90f);
        ci.cancel();
    }
}
