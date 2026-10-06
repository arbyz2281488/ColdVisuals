package com.example.visuals.mixin;

import com.example.visuals.modules.FreeLookModule;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    /** FreeLook: use the free camera angles instead of the player's own while the key is held. */
    @Redirect(method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"),
            require = 0)
    private void visuals$freeLookRotation(Camera instance, float yaw, float pitch) {
        if (FreeLookModule.active) {
            this.setRotation(FreeLookModule.yaw, FreeLookModule.pitch);
        } else {
            this.setRotation(yaw, pitch);
        }
    }
}
