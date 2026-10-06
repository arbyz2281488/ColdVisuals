package com.example.visuals.mixin;

import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.ViewModelModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), require = 0)
    private void visuals$viewModelPush(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                                       float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                                       VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ViewModelModule v = ModuleManager.VIEWMODEL;
        if (!v.enabled) return;
        float side = hand == Hand.MAIN_HAND ? 1f : -1f;
        matrices.push();
        matrices.translate((float) v.x.value * side, (float) v.y.value, (float) v.z.value);
        float s = (float) v.scale.value;
        matrices.scale(s, s, s);
        v.pushed++;
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"), require = 0)
    private void visuals$viewModelPop(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                                      float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                                      VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ViewModelModule v = ModuleManager.VIEWMODEL;
        if (v.pushed > 0) {
            matrices.pop();
            v.pushed--;
        }
    }
}
