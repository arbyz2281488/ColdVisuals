package com.example.visuals.mixin;

import com.example.visuals.TextFix;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashTextRenderer.class)
public class SplashTextRendererMixin {
    @Shadow
    @Final
    @Mutable
    private String text;

    /** MenuStyle: show splash lines written as "\\u0430..." escapes as normal readable text. */
    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"), require = 0)
    private void visuals$decodeSplash(String text, CallbackInfo ci) {
        if (ModuleManager.MENUSTYLE.fixSplash.asBool()) {
            this.text = TextFix.decodeUnicodeEscapes(this.text);
        }
    }
}
