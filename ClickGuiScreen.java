package com.example.visuals.gui;

import com.example.visuals.Config;
import com.example.visuals.modules.Module;
import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Left click on a module = toggle. Right click = expand/collapse its settings.
 * Mouse wheel scrolls the list. Click or drag on a setting row to change it.
 * HUD elements (highlighted with a frame) can be dragged with the mouse.
 */
public class ClickGuiScreen extends Screen {
    private static final int X = 20, Y = 20, W = 160, ROW = 16;
    private static final int FRAME = 0xFFB06CFF;

    private record Row(Module module, Setting setting, int y) {}

    private final Set<Module> expanded = new HashSet<>();
    private Setting dragging;
    private Module draggingHud;
    private int scroll = 0;

    public ClickGuiScreen() { super(Text.literal("Visuals")); }

    private int regionTop() { return Y + ROW; }
    private int regionBottom() { return height - 34; }

    /** Row positions without scrolling. */
    private List<Row> rows() {
        List<Row> list = new ArrayList<>();
        int y = regionTop();
        for (Module m : ModuleManager.all()) {
            list.add(new Row(m, null, y));
            y += ROW;
            if (expanded.contains(m)) {
                for (Setting s : m.settings) {
                    list.add(new Row(m, s, y));
                    y += ROW;
                }
            }
        }
        return list;
    }

    private void clampScroll() {
        int contentH = rows().size() * ROW;
        int max = Math.max(0, contentH - (regionBottom() - regionTop()));
        scroll = Math.max(0, Math.min(scroll, max));
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private boolean inRegion(double my) {
        return my >= regionTop() && my < regionBottom();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // No super.render(): avoids double background blur; we draw our own dim layer.
        ctx.fill(0, 0, width, height, 0x66000000);
        clampScroll();

        for (Module m : ModuleManager.all()) {
            if (m.enabled && m.movable && m.boxW > 0) {
                ctx.drawBorder(m.boxX - 2, m.boxY - 2, m.boxW + 4, m.boxH + 4, FRAME);
            }
        }

        ctx.fill(X, Y, X + W, Y + ROW, 0xFF111111);
        ctx.drawTextWithShadow(textRenderer, "Visuals", X + 5, Y + 4, 0xFFFFFFFF);
        String hint = "колесо: прокрутка";
        ctx.drawTextWithShadow(textRenderer, hint, X + W - 5 - textRenderer.getWidth(hint), Y + 4, 0xFF777777);

        String tooltip = null;
        ctx.enableScissor(X, regionTop(), X + W, regionBottom());
        for (Row row : rows()) {
            int ry = row.y - scroll;
            if (ry + ROW <= regionTop() || ry >= regionBottom()) continue;
            boolean hover = inRegion(mouseY) && inside(mouseX, mouseY, X, ry, W, ROW);
            if (row.setting == null) {
                Module m = row.module;
                int bg = m.enabled ? 0xFF2E7D32 : (hover ? 0xFF3C3C3C : 0xFF2A2A2A);
                ctx.fill(X, ry, X + W, ry + ROW, bg);
                ctx.drawTextWithShadow(textRenderer, m.name, X + 5, ry + 4, 0xFFFFFFFF);
                if (!m.settings.isEmpty()) {
                    ctx.drawTextWithShadow(textRenderer, expanded.contains(m) ? "-" : "+", X + W - 10, ry + 4, 0xFFAAAAAA);
                }
                if (hover) tooltip = m.description + "  (RMB: settings)";
            } else {
                Setting s = row.setting;
                ctx.fill(X, ry, X + W, ry + ROW, 0xFF181818);
                if (s.type == Setting.Type.BOOL) {
                    ctx.drawTextWithShadow(textRenderer, s.name, X + 10, ry + 4, 0xFFDDDDDD);
                    ctx.drawTextWithShadow(textRenderer, s.asBool() ? "ON" : "OFF",
                            X + W - 24, ry + 4, s.asBool() ? 0xFF66FF66 : 0xFFFF6666);
                } else {
                    double frac = (s.value - s.min) / (s.max - s.min);
                    ctx.fill(X, ry, X + (int) (W * frac), ry + ROW, 0xFF1565C0);
                    ctx.drawTextWithShadow(textRenderer,
                            s.name + ": " + (Math.round(s.value * 10) / 10.0), X + 10, ry + 4, 0xFFFFFFFF);
                }
            }
        }
        ctx.disableScissor();

        if (tooltip != null) {
            ctx.drawTextWithShadow(textRenderer, tooltip, mouseX + 8, mouseY - 4, 0xFFFFFF55);
        }
        ctx.drawTextWithShadow(textRenderer, "Тяни элементы в рамке мышкой, чтобы переместить",
                X, height - 26, 0xFFBBBBBB);
        ctx.drawTextWithShadow(textRenderer, "Esc / Right Shift - закрыть", X, height - 14, 0xFF888888);
    }

    private void applySlider(Setting s, double mouseX) {
        double frac = Math.max(0, Math.min(1, (mouseX - X) / W));
        s.value = Math.round((s.min + frac * (s.max - s.min)) * 10) / 10.0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inRegion(mouseY)) {
            for (Row row : rows()) {
                int ry = row.y - scroll;
                if (!inside(mouseX, mouseY, X, ry, W, ROW)) continue;
                if (row.setting == null) {
                    if (button == 0) row.module.toggle();
                    else if (button == 1 && !row.module.settings.isEmpty()) {
                        if (!expanded.remove(row.module)) expanded.add(row.module);
                    }
                } else if (row.setting.type == Setting.Type.BOOL) {
                    row.setting.value = row.setting.asBool() ? 0 : 1;
                } else {
                    dragging = row.setting;
                    applySlider(dragging, mouseX);
                }
                return true;
            }
        }
        // not on the panel: try to grab a HUD element (topmost = last in list)
        if (button == 0) {
            List<Module> mods = ModuleManager.all();
            for (int i = mods.size() - 1; i >= 0; i--) {
                Module m = mods.get(i);
                if (m.enabled && m.hitBox(mouseX, mouseY)) {
                    draggingHud = m;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging != null) {
            applySlider(dragging, mouseX);
            return true;
        }
        if (draggingHud != null) {
            draggingHud.moveBy(dx, dy);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = null;
        draggingHud = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= (int) Math.round(verticalAmount * ROW);
        clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() { Config.save(); }

    @Override
    public boolean shouldPause() { return false; }
}
