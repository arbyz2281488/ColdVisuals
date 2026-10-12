package com.example.visuals.gui;

import com.example.visuals.ErrorLog;
import com.example.visuals.Modrinth;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** In-game Modrinth browser: search mods / resource packs / shaders and install them with one click. */
public class ModBrowserScreen extends Screen {
    private enum Kind { TAB, INPUT, FIND, DOWNLOAD, FOLDER, BACK }

    private record Hit(Kind kind, String value, Modrinth.Hit project, int x, int y, int w, int h) {}

    private static final String[] TYPES = {Modrinth.MOD, Modrinth.PACK, Modrinth.SHADER};
    private static final String[] TYPE_NAMES = {"Моды", "Ресурспаки", "Шейдеры"};

    private final Screen parent;
    private final List<Hit> hits = new ArrayList<>();
    private final Set<String> working = ConcurrentHashMap.newKeySet();
    private final Map<String, String> done = new ConcurrentHashMap<>();

    private String type = Modrinth.MOD;
    private String query = "";
    private boolean focused = true;
    private volatile List<Modrinth.Hit> results = List.of();
    private volatile String status = "Загружаю популярное...";
    private volatile boolean searching;
    private int scroll;

    public ModBrowserScreen(Screen parent) {
        super(Text.literal("Моды"));
        this.parent = parent;
        search();
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void search() {
        if (searching) return;
        searching = true;
        status = "Ищу...";
        scroll = 0;
        final String q = query.trim();
        final String t = type;
        Thread th = new Thread(() -> {
            try {
                List<Modrinth.Hit> r = Modrinth.search(q, t, q.isEmpty());
                results = r;
                status = r.isEmpty() ? "Ничего не найдено" : "Найдено: " + r.size();
            } catch (Throwable e) {
                ErrorLog.write("ModBrowser.search", e);
                status = "Ошибка поиска: " + e.getMessage();
            } finally {
                searching = false;
            }
        }, "cold-modrinth-search");
        th.setDaemon(true);
        th.start();
    }

    private void install(Modrinth.Hit h) {
        if (!working.add(h.id())) return;
        final String t = type;
        Thread th = new Thread(() -> {
            try {
                String msg = Modrinth.install(h, t, s -> status = s);
                done.put(h.id(), msg);
                status = msg + (t.equals(Modrinth.MOD) ? "  Перезапусти игру." : "");
            } catch (Throwable e) {
                ErrorLog.write("ModBrowser.install", e);
                status = "Ошибка: " + e.getMessage();
            } finally {
                working.remove(h.id());
            }
        }, "cold-modrinth-install");
        th.setDaemon(true);
        th.start();
    }

    private static String fmt(long n) {
        if (n >= 1_000_000) return String.format(Locale.ROOT, "%.1fM", n / 1_000_000.0);
        if (n >= 1_000) return String.format(Locale.ROOT, "%.1fK", n / 1_000.0);
        return String.valueOf(n);
    }

    private String trim(String s, int w) {
        return textRenderer.getWidth(s) <= w ? s : textRenderer.trimToWidth(s, w - 6) + "..";
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        try {
            draw(ctx, mouseX, mouseY);
        } catch (Throwable t) {
            ErrorLog.write("ModBrowserScreen.render", t);
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

        int ww = Math.min(width - 20, 380), wh = Math.min(height - 20, 252);
        int wx = (width - ww) / 2, wy = (height - wh) / 2;
        RenderUtil.roundedRect(ctx, wx - 1, wy - 1, ww + 2, wh + 2, 9, (0x60 << 24) | (acc & 0xFFFFFF));
        RenderUtil.roundedGradient(ctx, wx, wy, ww, wh, 8, 0xF21A2A48, 0xF20A101E);

        // tabs
        int tx = wx + 12;
        for (int i = 0; i < TYPES.length; i++) {
            int tw = textRenderer.getWidth(TYPE_NAMES[i]) + 16;
            boolean sel = TYPES[i].equals(type);
            boolean hov = inside(mouseX, mouseY, tx, wy + 8, tw, 18);
            RenderUtil.roundedRect(ctx, tx, wy + 8, tw, 18, 6, sel ? acc : (hov ? 0xFF24406E : 0xFF1A2E52));
            ctx.drawTextWithShadow(textRenderer, TYPE_NAMES[i], tx + 8, wy + 13, sel ? 0xFF0A1424 : 0xFFD6E6F8);
            hits.add(new Hit(Kind.TAB, TYPES[i], null, tx, wy + 8, tw, 18));
            tx += tw + 6;
        }
        String ver = "Fabric " + Modrinth.gameVersion();
        ctx.drawTextWithShadow(textRenderer, ver, wx + ww - 12 - textRenderer.getWidth(ver), wy + 13, 0xFF8FA9CC);

        // search row
        int ix = wx + 12, iy = wy + 32, iw = ww - 24 - 62;
        RenderUtil.roundedRect(ctx, ix - 1, iy - 1, iw + 2, 22, 6, focused ? acc : 0x336CC8FF);
        RenderUtil.roundedRect(ctx, ix, iy, iw, 20, 5, 0xFF0C1424);
        boolean empty = query.isEmpty() && !focused;
        String shown = empty ? "Поиск на Modrinth..." : query + (focused && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        ctx.drawTextWithShadow(textRenderer, shown, ix + 7, iy + 6, empty ? 0xFF5F6F86 : 0xFFFFFFFF);
        hits.add(new Hit(Kind.INPUT, "", null, ix, iy, iw, 20));
        int fx = ix + iw + 6;
        boolean hf = inside(mouseX, mouseY, fx, iy, 56, 20);
        RenderUtil.roundedRect(ctx, fx, iy, 56, 20, 5, hf ? 0xFF8FD8FF : acc);
        ctx.drawTextWithShadow(textRenderer, "Найти", fx + (56 - textRenderer.getWidth("Найти")) / 2, iy + 6, 0xFF0A1424);
        hits.add(new Hit(Kind.FIND, "", null, fx, iy, 56, 20));

        // results
        int ly = wy + 60, lh = wh - 60 - 46, rowH = 36;
        List<Modrinth.Hit> list = results;
        int max = Math.max(0, list.size() * rowH - lh);
        scroll = Math.max(0, Math.min(scroll, max));

        ctx.enableScissor(wx + 8, ly, wx + ww - 8, ly + lh);
        if (list.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, searching ? "Загрузка..." : "Здесь появятся результаты.", wx + 14, ly + 8, 0xFF6C7C94);
        }
        for (int i = 0; i < list.size(); i++) {
            Modrinth.Hit h = list.get(i);
            int ry = ly + i * rowH - scroll;
            if (ry + rowH < ly || ry > ly + lh) continue;
            boolean hover = inside(mouseX, mouseY, wx + 12, ry, ww - 24, rowH - 4) && inside(mouseX, mouseY, wx + 8, ly, ww - 16, lh);
            RenderUtil.roundedGradient(ctx, wx + 12, ry, ww - 24, rowH - 4, 6, hover ? 0xFF1C3050 : 0xFF152238, 0xFF101A2C);

            int textW = ww - 24 - 80;
            ctx.drawTextWithShadow(textRenderer, trim(h.title(), textW), wx + 20, ry + 4, 0xFFFFFFFF);
            ctx.drawTextWithShadow(textRenderer, trim(h.author() + "  |  " + fmt(h.downloads()) + " скачиваний", textW),
                    wx + 20, ry + 14, 0xFF8FA9CC);
            ctx.drawTextWithShadow(textRenderer, trim(h.description(), textW), wx + 20, ry + 24 - 2, 0xFF6C7C94);

            boolean busy = working.contains(h.id());
            boolean installed = type.equals(Modrinth.MOD) && Modrinth.isInstalled(h.slug());
            boolean got = done.containsKey(h.id());
            String label = busy ? "..." : (got ? "Готово" : (installed ? "Есть" : "Скачать"));
            boolean canClick = !busy && !got && !installed;
            int bw = 62, bx = wx + ww - 12 - bw - 6, by = ry + 7;
            boolean hb = canClick && inside(mouseX, mouseY, bx, by, bw, 18);
            RenderUtil.roundedRect(ctx, bx, by, bw, 18, 5, canClick ? (hb ? 0xFF8FD8FF : acc) : 0xFF2A3A55);
            ctx.drawTextWithShadow(textRenderer, label, bx + (bw - textRenderer.getWidth(label)) / 2, by + 5,
                    canClick ? 0xFF0A1424 : 0xFFA8B8D0);
            if (canClick) hits.add(new Hit(Kind.DOWNLOAD, "", h, bx, by, bw, 18));
        }
        ctx.disableScissor();

        // bottom
        int by = wy + wh - 40;
        int half = (ww - 24 - 6) / 2;
        boolean h1 = inside(mouseX, mouseY, wx + 12, by, half, 20);
        RenderUtil.roundedRect(ctx, wx + 12, by, half, 20, 5, h1 ? 0xFF24406E : 0xFF1A2E52);
        String f = "Открыть папку";
        ctx.drawTextWithShadow(textRenderer, f, wx + 12 + (half - textRenderer.getWidth(f)) / 2, by + 6, 0xFFE0ECFA);
        hits.add(new Hit(Kind.FOLDER, "", null, wx + 12, by, half, 20));
        boolean h2 = inside(mouseX, mouseY, wx + 18 + half, by, half, 20);
        RenderUtil.roundedRect(ctx, wx + 18 + half, by, half, 20, 5, h2 ? 0xFF24406E : 0xFF1A2E52);
        ctx.drawTextWithShadow(textRenderer, "Назад", wx + 18 + half + (half - textRenderer.getWidth("Назад")) / 2, by + 6, 0xFFE0ECFA);
        hits.add(new Hit(Kind.BACK, "", null, wx + 18 + half, by, half, 20));

        String st = status;
        ctx.drawTextWithShadow(textRenderer, trim(st, ww - 24), wx + 12, wy + wh - 14, 0xFF66E08A);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        try {
            return handleClick(mx, my, button);
        } catch (Throwable t) {
            ErrorLog.write("ModBrowserScreen.click", t);
            status = "Ошибка: " + t.getClass().getSimpleName();
            return true;
        }
    }

    private boolean handleClick(double mx, double my, int button) {
        focused = false;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!inside(mx, my, h.x(), h.y(), h.w(), h.h())) continue;
            switch (h.kind()) {
                case TAB -> {
                    type = h.value();
                    results = List.of();
                    search();
                }
                case INPUT -> focused = true;
                case FIND -> search();
                case DOWNLOAD -> install(h.project());
                case FOLDER -> {
                    try {
                        Modrinth.openFolder(type);
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
    public boolean mouseScrolled(double mx, double my, double horizontalAmount, double verticalAmount) {
        scroll -= (int) Math.round(verticalAmount * 18);
        if (scroll < 0) scroll = 0;
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (focused && chr >= 32 && chr != 127 && query.length() < 40) {
            query += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!query.isEmpty()) query = query.substring(0, query.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                search();
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
