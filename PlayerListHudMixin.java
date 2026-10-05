package com.example.visuals.mixin;

import com.example.visuals.modules.ColdTagModule;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true, require = 0)
    private void visuals$coldTag(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        ColdTagModule m = ModuleManager.COLDTAG;
        if (!m.enabled || !m.tab.asBool()) return;
        if (m.isColdUser(entry.getProfile().getName())) {
            cir.setReturnValue(ColdTagModule.decorate(cir.getReturnValue()));
        }
    }
}
