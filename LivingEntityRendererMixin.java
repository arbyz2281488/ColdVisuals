package com.example.visuals.mixin;

import com.example.visuals.modules.BabyLolModule;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    /** SeeOwnName: show your own nametag in third person. */
    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("RETURN"), cancellable = true, require = 0)
    private void visuals$ownName(LivingEntity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (ModuleManager.SEEOWNNAME.enabled && entity == mc.player && !mc.options.getPerspective().isFirstPerson()) {
            cir.setReturnValue(true);
        }
    }

    /** BabyLol: draw players smaller (only how they look; hitboxes do not change). */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), require = 0)
    private void visuals$babyPush(LivingEntityRenderState state, MatrixStack matrices,
                                  VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        BabyLolModule m = ModuleManager.BABYLOL;
        if (!m.enabled || !(state instanceof PlayerEntityRenderState)) return;
        float s = (float) m.scale.value;
        matrices.push();
        matrices.scale(s, s, s);
        m.pushed++;
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("RETURN"), require = 0)
    private void visuals$babyPop(LivingEntityRenderState state, MatrixStack matrices,
                                 VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        BabyLolModule m = ModuleManager.BABYLOL;
        if (m.pushed > 0 && state instanceof PlayerEntityRenderState) {
            matrices.pop();
            m.pushed--;
        }
    }
}
