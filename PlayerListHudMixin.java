package com.example.visuals.mixin;

import com.example.visuals.modules.ColdTagModule;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    /** ColdTag: [Cold] in front of nicknames of Cold users. */
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true, require = 0)
    private void visuals$coldTag(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        ColdTagModule m = ModuleManager.COLDTAG;
        if (!m.enabled || !m.tab.asBool()) return;
        if (m.isColdUser(entry.getProfile().getName())) {
            cir.setReturnValue(ColdTagModule.decorate(cir.getReturnValue()));
        }
    }

    /** FullTabList: the TAB list normally stops at 80 players. */
    @ModifyConstant(method = "collectPlayerEntries", constant = @Constant(longValue = 80L), require = 0)
    private long visuals$fullTab(long original) {
        return ModuleManager.FULLTAB.enabled ? 500L : original;
    }
}
