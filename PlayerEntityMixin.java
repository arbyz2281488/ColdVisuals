package com.example.visuals.mixin;

import com.example.visuals.modules.ColdTagModule;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, require = 0)
    private void visuals$coldTag(CallbackInfoReturnable<Text> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!self.getWorld().isClient()) return;
        ColdTagModule m = ModuleManager.COLDTAG;
        if (!m.enabled || !m.nametag.asBool()) return;
        if (m.isColdUser(self.getName().getString())) {
            cir.setReturnValue(ColdTagModule.decorate(cir.getReturnValue()));
        }
    }
}
