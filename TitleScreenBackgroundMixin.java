package com.example.visuals.mixin;

import com.example.visuals.gui.MenuBackground;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenBackgroundMixin {
    /** The title screen has its own version of the panorama method, so it needs its own hook. */
    @Inject(method = "renderPanoramaBackground(Lnet/minecraft/client/gui/DrawContext;F)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$menuBackground(DrawContext context, float delta, CallbackInfo ci) {
        if (!ModuleManager.MENUSTYLE.enabled) return;
        Screen self = (Screen) (Object) this;
        MenuBackground.drawBackground(context, self.width, self.height);
        ci.cancel();
    }
}
