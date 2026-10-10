package com.example.visuals.gui;

import com.example.visuals.AccountManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Ice-styled nickname manager: add, remove and switch offline accounts. */
public class AccountManagerScreen extends Screen {
    private static final int ACCENT = 0xFFB06CFF, ICE = 0xFF6CC8FF;

    private enum Kind { INPUT, ADD, RANDOM, BACK, ACCOUNT, REMOVE }

    private record Hit(Kind kind, String name, int x, int y, int w, int h) {}

    private final Screen parent;
    private final List<Hit> hits = new ArrayList<>();
    private String input = "";
    private boolean focused = true;
    private String status = "";
    private long statusUntil;
    private int scroll;

    public AccountManagerScreen(Screen parent) {
        super(Text.literal("Аккаунты"));
        this.parent = parent;
    }

    private void flash(String s) {
        status = s;
        statusUntil = System.currentTimeMillis() + 3000;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void addFromInput() {
        String name = input.trim();
        if (!AccountManager.validName(name)) {
            flash("Ник: 3-16 символов, буквы, цифры и _");
            return;
        }
        if (AccountManager.add(name)) {
            input = "";
            flash("Добавлен: " + name);
        } else {
            flash(name + " уже в списке");
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        hits.clear();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) MenuBackground.drawBackground(ctx, width, height);
        else ctx.fill(0, 0, width, height, 0xA0000000);

        int ww = Math.min(width - 20, 300), wh = Math.min(height - 20, 232);
        int wx = (width - ww) / 2, wy = (height - wh) / 2;

        RenderUtil.roundedRect(ctx, wx - 1, wy - 1, ww + 2, wh + 2, 9, 0x606CC8FF);
        RenderUtil.roundedRect(ctx, wx, wy, ww, wh, 8, 0xF0101828);

        ctx.drawTextWithShadow(textRenderer, "Аккаунты", wx + 12, wy + 10, ICE);
        String cur = "Сейчас: " + AccountManager.current();
        ctx.drawTextWithShadow(textRenderer, cur, wx + ww - 12 - textRenderer.getWidth(cur), wy + 10, 0xFFDDE8F6);

        // input row
        int ix = wx + 12, iy = wy + 28, iw = ww - 24 - 64;
        RenderUtil.roundedRect(ctx, ix - 1, iy - 1, iw + 2, 22, 6, focused ? ICE : 0x336CC8FF);
        RenderUtil.roundedRect(ctx, ix, iy, iw, 20, 5, 0xFF0C1424);
        boolean empty = input.isEmpty() && !focused;
        String shown = empty ? "Новый ник..." : input + (focused && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        ctx.drawTextWithShadow(textRenderer, shown, ix + 7, iy + 6, empty ? 0xFF5F6F86 : 0xFFFFFFFF);
        hits.add(new Hit(Kind.INPUT, "", ix, iy, iw, 20));

        int bx = ix + iw + 6, bw = 58;
        boolean hAdd = inside(mouseX, mouseY, bx, iy, bw, 20);
        RenderUtil.roundedRect(ctx, bx, iy, bw, 20, 5, hAdd ? 0xFF8FD8FF : ICE);
        ctx.drawTextWithShadow(textRenderer, "Добавить", bx + (bw - textRenderer.getWidth("Добавить")) / 2, iy + 6, 0xFF0A1424);
        hits.add(new Hit(Kind.ADD, "", bx, iy, bw, 20));

        // list
        int ly = wy + 58, lh = wh - 58 - 50;
        List<String> names = new ArrayList<>(AccountManager.all());
        int rowH = 22;
        int max = Math.max(0, names.size() * rowH - lh);
        scroll = Math.max(0, Math.min(scroll, max));
        String curName = AccountManager.current();

        ctx.enableScissor(wx + 8, ly, wx + ww - 8, ly + lh);
        if (names.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, "Список пуст. Введи ник выше или нажми «Случайный».", wx + 14, ly + 6, 0xFF6C7C94);
        }
        for (int i = 0; i < names.size(); i++) {
            String n = names.get(i);
            int ry = ly + i * rowH - scroll;
            if (ry + rowH < ly || ry > ly + lh) continue;
            boolean hover = inside(mouseX, mouseY, wx + 12, ry, ww - 24, 20) && inside(mouseX, mouseY, wx + 8, ly, ww - 16, lh);
            boolean isCur = n.equals(curName);
            RenderUtil.roundedRect(ctx, wx + 12, ry, ww - 24, 20, 6, isCur ? 0xFF1E3A66 : (hover ? 0xFF182A48 : 0xFF121E34));
            if (isCur) RenderUtil.roundedRect(ctx, wx + 12, ry + 4, 2, 12, 1, ICE);
            ctx.drawTextWithShadow(textRenderer, n, wx + 22, ry + 6, isCur ? 0xFFFFFFFF : 0xFFC8D8EE);
            if (isCur) ctx.drawTextWithShadow(textRenderer, "используется", wx + ww - 58 - textRenderer.getWidth("используется"), ry + 6, 0xFF66E08A);
            boolean hx = hover && mouseX >= wx + ww - 40;
            RenderUtil.roundedRect(ctx, wx + ww - 38, ry + 2, 16, 16, 5, hx ? 0xFF8A2A3A : 0xFF22304A);
            ctx.drawTextWithShadow(textRenderer, "x", wx + ww - 38 + 5, ry + 6, hx ? 0xFFFFFFFF : 0xFFAAB8CC);
            if (ry >= ly - rowH && ry <= ly + lh) {
                hits.add(new Hit(Kind.ACCOUNT, n, wx + 12, ry, ww - 24 - 28, 20));
                hits.add(new Hit(Kind.REMOVE, n, wx + ww - 38, ry + 2, 16, 16));
            }
        }
        ctx.disableScissor();

        // bottom buttons
        int by = wy + wh - 42;
        int half = (ww - 24 - 6) / 2;
        boolean hr = inside(mouseX, mouseY, wx + 12, by, half, 20);
        RenderUtil.roundedRect(ctx, wx + 12, by, half, 20, 5, hr ? 0xFF24406E : 0xFF1A2E52);
        ctx.drawTextWithShadow(textRenderer, "Случайный ник", wx + 12 + (half - textRenderer.getWidth("Случайный ник")) / 2, by + 6, 0xFFE0ECFA);
        hits.add(new Hit(Kind.RANDOM, "", wx + 12, by, half, 20));

        boolean hb = inside(mouseX, mouseY, wx + 18 + half, by, half, 20);
        RenderUtil.roundedRect(ctx, wx + 18 + half, by, half, 20, 5, hb ? 0xFF24406E : 0xFF1A2E52);
        ctx.drawTextWithShadow(textRenderer, "Назад", wx + 18 + half + (half - textRenderer.getWidth("Назад")) / 2, by + 6, 0xFFE0ECFA);
        hits.add(new Hit(Kind.BACK, "", wx + 18 + half, by, half, 20));

        boolean flashing = System.currentTimeMillis() < statusUntil;
        String foot = flashing ? status : "Ник меняется при следующем входе на сервер. Только для серверов без проверки лицензии.";
        ctx.drawTextWithShadow(textRenderer, foot.length() > 0 && textRenderer.getWidth(foot) > ww - 20
                ? textRenderer.trimToWidth(foot, ww - 24) : foot, wx + 12, wy + wh - 15, flashing ? 0xFF66E08A : 0xFF5F6F86);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        focused = false;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!inside(mx, my, h.x(), h.y(), h.w(), h.h())) continue;
            switch (h.kind()) {
                case INPUT -> focused = true;
                case ADD -> addFromInput();
                case RANDOM -> {
                    String n = AccountManager.randomName();
                    AccountManager.add(n);
                    AccountManager.use(n);
                    flash("Создан и выбран: " + n);
                }
                case BACK -> close();
                case ACCOUNT -> {
                    AccountManager.use(h.name());
                    flash("Выбран: " + h.name());
                }
                case REMOVE -> {
                    AccountManager.remove(h.name());
                    flash("Удалён: " + h.name());
                }
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontalAmount, double verticalAmount) {
        scroll -= (int) Math.round(verticalAmount * 16);
        if (scroll < 0) scroll = 0;
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (focused) {
            boolean ok = (chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z') || (chr >= '0' && chr <= '9') || chr == '_';
            if (ok && input.length() < 16) input += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!input.isEmpty()) input = input.substring(0, input.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                addFromInput();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
