package com.example.visuals.mixin;

import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.SmoothChatModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void visuals$chatPush(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        SmoothChatModule m = ModuleManager.SMOOTHCHAT;
        if (!m.enabled) return;
        float off = m.offset();
        if (off <= 0.01f) return;
        context.getMatrices().push();
        context.getMatrices().translate(0f, off, 0f);
        m.pushed = true;
    }

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    private void visuals$chatPop(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        SmoothChatModule m = ModuleManager.SMOOTHCHAT;
        if (m.pushed) {
            context.getMatrices().pop();
            m.pushed = false;
        }
    }
}
