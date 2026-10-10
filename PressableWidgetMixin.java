package com.example.visuals.mixin;

import com.example.visuals.gui.RenderUtil;
import com.example.visuals.modules.ButtonStyleModule;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PressableWidget.class)
public class PressableWidgetMixin {
    private static int lerp(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (((c1 >>> 24) & 255) * (1 - t) + ((c2 >>> 24) & 255) * t);
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** ButtonStyle: draw normal buttons (and cycling option buttons) in the Cold style. */
    @Inject(method = "renderWidget(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void visuals$buttonStyle(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ButtonStyleModule m = ModuleManager.BUTTONSTYLE;
        if (!m.enabled) return;
        PressableWidget self = (PressableWidget) (Object) this;
        if (!(self instanceof ButtonWidget) && !(self instanceof CyclingButtonWidget<?>)) return;

        int x = self.getX(), y = self.getY(), w = self.getWidth(), h = self.getHeight();
        boolean active = self.active;
        float k = active ? m.hover(self, self.isHovered()) : 0f;

        int border = active ? lerp(0x553C7CC8, m.glow.asBool() ? 0xFF6CC8FF : 0xAA6CC8FF, k) : 0x33405060;
        int fill = active ? lerp(0xD0102038, 0xE01C3862, k) : 0x99202834;
        RenderUtil.roundedRect(context, x - 1, y - 1, w + 2, h + 2, 6, border);
        RenderUtil.roundedRect(context, x, y, w, h, 5, fill);

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        Text msg = self.getMessage();
        int color = active ? lerp(0xFFD0E2F6, 0xFFFFFFFF, k) : 0xFF7A8696;
        context.drawTextWithShadow(tr, msg, x + (w - tr.getWidth(msg)) / 2, y + (h - 8) / 2, color);
        ci.cancel();
    }
}
