package com.example.visuals.gui;

import com.example.visuals.ErrorLog;
import com.example.visuals.ModelPack;
import com.example.visuals.ModelPackData;
import com.example.visuals.Modrinth;
import com.example.visuals.Recolor;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Choose which weapon model and which colour variant each sword (and the bow) uses. */
public class ModelsScreen extends Screen {
    private enum Kind { MODEL, VARIANT, APPLY, FOLDER, BACK }

    private record Hit(Kind kind, int row, int x, int y, int w, int h) {}

    private final Screen parent;
    private final List<Hit> hits = new ArrayList<>();
    private String status = "";

    public ModelsScreen(Screen parent) {
        super(Text.literal("Модели"));
        this.parent = parent;
        ModelPack.load();
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private String trim(String s, int w) {
        return textRenderer.getWidth(s) <= w ? s : textRenderer.trimToWidth(s, w - 6) + "..";
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        try {
            draw(ctx, mouseX, mouseY);
        } catch (Throwable t) {
            ErrorLog.write("ModelsScreen.render", t);
            ctx.fill(0, 0, width, height, 0xFF101828);
            ctx.drawTextWithShadow(textRenderer, "Ошибка: " + t, 12, 12, 0xFFFF6B6B);
            ctx.drawTextWithShadow(textRenderer, "Подробности в config/visuals-error.log. Esc - назад.", 12, 28, 0xFFCCCCCC);
        }
    }

    private void draw(DrawContext ctx, int mouseX, int mouseY) {
        hits.clear();
        int acc = ModuleManager.THEME.accent();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) MenuBackground.drawBackground(ctx, width, height);
        else ctx.fill(0, 0, width, height, 0xA0000000);

        int ww = Math.min(width - 20, 380), wh = Math.min(height - 20, 262);
        int wx = (width - ww) / 2, wy = (height - wh) / 2;
        RenderUtil.roundedRect(ctx, wx - 1, wy - 1, ww + 2, wh + 2, 9, (0x60 << 24) | (acc & 0xFFFFFF));
        RenderUtil.roundedGradient(ctx, wx, wy, ww, wh, 8, 0xF21A2A48, 0xF20A101E);

        ctx.drawTextWithShadow(textRenderer, "Модели оружия", wx + 12, wy + 10, acc);
        String hint = "ЛКМ - дальше, ПКМ - назад";
        ctx.drawTextWithShadow(textRenderer, hint, wx + ww - 12 - textRenderer.getWidth(hint), wy + 10, 0xFF6C7C94);

        int nameW = 108, modelX = wx + 12 + nameW, modelW = 134, varX = modelX + modelW + 6, varW = ww - 12 - (varX - wx);
        ctx.drawTextWithShadow(textRenderer, "Предмет", wx + 14, wy + 28, 0xFF8FA9CC);
        ctx.drawTextWithShadow(textRenderer, "Модель", modelX + 6, wy + 28, 0xFF8FA9CC);
        ctx.drawTextWithShadow(textRenderer, "Текстура", varX + 6, wy + 28, 0xFF8FA9CC);

        for (int i = 0; i < ModelPack.ITEMS.length; i++) {
            int ry = wy + 40 + i * 24;
            RenderUtil.roundedRect(ctx, wx + 8, ry, ww - 16, 22, 6, 0xFF111C30);
            ctx.drawTextWithShadow(textRenderer, ModelPack.ITEM_NAMES[i], wx + 14, ry + 7, 0xFFDDE8F6);

            int m = ModelPack.model[i];
            String mt = m < 0 ? "Обычный" : ModelPackData.NAMES[m];
            boolean hm = inside(mouseX, mouseY, modelX, ry + 2, modelW, 18);
            RenderUtil.roundedRect(ctx, modelX, ry + 2, modelW, 18, 5, hm ? 0xFF24406E : 0xFF1A2E52);
            ctx.drawTextWithShadow(textRenderer, trim(mt, modelW - 8), modelX + 6, ry + 7, m < 0 ? 0xFF8FA9CC : 0xFFFFFFFF);
            hits.add(new Hit(Kind.MODEL, i, modelX, ry + 2, modelW, 18));

            boolean on = m >= 0;
            boolean hv = on && inside(mouseX, mouseY, varX, ry + 2, varW, 18);
            RenderUtil.roundedRect(ctx, varX, ry + 2, varW, 18, 5, !on ? 0xFF141E30 : (hv ? 0xFF24406E : 0xFF1A2E52));
            ctx.drawTextWithShadow(textRenderer, trim(ModelPack.VARIANTS[ModelPack.variant[i]], varW - 8), varX + 6, ry + 7,
                    on ? 0xFFFFFFFF : 0xFF4A5A74);
            if (on) hits.add(new Hit(Kind.VARIANT, i, varX, ry + 2, varW, 18));
        }

        int by = wy + wh - 46;
        int third = (ww - 24 - 12) / 3;
        String[] labels = {"Применить", "Папка паков", "Назад"};
        Kind[] kinds = {Kind.APPLY, Kind.FOLDER, Kind.BACK};
        for (int k = 0; k < 3; k++) {
            int bx = wx + 12 + k * (third + 6);
            boolean h = inside(mouseX, mouseY, bx, by, third, 20);
            int col = k == 0 ? (h ? 0xFF8FD8FF : acc) : (h ? 0xFF24406E : 0xFF1A2E52);
            RenderUtil.roundedRect(ctx, bx, by, third, 20, 5, col);
            ctx.drawTextWithShadow(textRenderer, labels[k], bx + (third - textRenderer.getWidth(labels[k])) / 2, by + 6,
                    k == 0 ? 0xFF0A1424 : 0xFFE0ECFA);
            hits.add(new Hit(kinds[k], 0, bx, by, third, 20));
        }

        String foot = status.isEmpty() ? "Один раз включи пак «ColdModels» в Настройки - Ресурспаки." : status;
        ctx.drawTextWithShadow(textRenderer, trim(foot, ww - 24), wx + 12, wy + wh - 16, status.isEmpty() ? 0xFF6C7C94 : 0xFF66E08A);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        try {
            return handleClick(mx, my, button);
        } catch (Throwable t) {
            ErrorLog.write("ModelsScreen.click", t);
            status = "Ошибка: " + t.getClass().getSimpleName();
            return true;
        }
    }

    private boolean handleClick(double mx, double my, int button) {
        int dir = button == 1 ? -1 : 1;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!inside(mx, my, h.x(), h.y(), h.w(), h.h())) continue;
            switch (h.kind()) {
                case MODEL -> {
                    int n = ModelPackData.SLUGS.length + 1; // +1 = vanilla
                    int cur = ModelPack.model[h.row()] + 1;
                    ModelPack.model[h.row()] = Math.floorMod(cur + dir, n) - 1;
                }
                case VARIANT -> ModelPack.variant[h.row()] = Math.floorMod(ModelPack.variant[h.row()] + dir, Recolor.COUNT);
                case APPLY -> {
                    try {
                        ModelPack.apply();
                        MinecraftClient.getInstance().reloadResources();
                        status = "Применено. Если не видно, включи пак ColdModels в ресурспаках.";
                    } catch (Exception e) {
                        ErrorLog.write("ModelsScreen.apply", e);
                        status = "Ошибка: " + e.getMessage();
                    }
                }
                case FOLDER -> {
                    try {
                        Modrinth.openFolder(Modrinth.PACK);
                    } catch (Exception e) {
                        status = "Не удалось открыть папку: " + e.getMessage();
                    }
                }
                case BACK -> close();
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void close() {
        ModelPack.save();
        MinecraftClient.getInstance().setScreen(parent);
    }
}
