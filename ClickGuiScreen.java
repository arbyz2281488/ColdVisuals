package com.example.visuals.gui;

import com.example.visuals.BindManager;
import com.example.visuals.Config;
import com.example.visuals.FriendManager;
import com.example.visuals.modules.Module;
import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Cold GUI. Left: tabs. Right: module cards (click = on/off, right click or "..." = settings).
 * Settings page: switches, sliders and key binds. Search box on top. Wheel scrolls.
 * "Friends" tab: add friends by nickname or by clicking a player on the server, protect them from your hits.
 * HUD elements (purple frames) can be dragged with the mouse outside the window.
 */
public class ClickGuiScreen extends Screen {
    private static final String[] TABS = {"Interface", "Helper", "Visuals", "Other", "Friends"};
    private static final int[] TAB_COLORS = {0xFFB06CFF, 0xFF6C9CFF, 0xFFFF7AC6, 0xFF9AA0B8, 0xFF66E08A};
    private static final String FRIENDS = "Friends";

    private static final int ACCENT = 0xFFB06CFF;
    private static final int PANEL = 0xFF1A1A27;
    private static final int PANEL_ON = 0xFF241E38;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFF8E8EA8;
    private static final int SIDE = 96;

    private enum Kind { CARD, TOGGLE_ROW, BOOL, SLIDER, KEY, PROTECT, INPUT, SECTION, HINT, FRIEND, PLAYER }

    private record Item(Kind kind, Module m, Setting s, int idx, String label, int x, int y, int w, int h) {}

    private final long openedAt = System.currentTimeMillis();
    private final List<Item> items = new ArrayList<>();
    private final Map<Module, Float> hoverAnim = new HashMap<>();

    private String category = "Interface";
    private Module settingsOf;
    private String query = "";
    private boolean searchFocused;
    private String friendInput = "";
    private boolean friendFocused;
    private String status = "";
    private long statusUntil;
    private int scroll;
    private int contentHeight;

    private Setting dragSetting;
    private int dragX, dragW;
    private Module draggingHud;
    private Module listening;
    private int listeningIdx;

    // geometry (recomputed every frame / event)
    private float alpha = 1f;
    private int wx, wy, ww, wh;
    private int cx, cy, cw, ch;

    public ClickGuiScreen() { super(Text.literal("Cold")); }

    // ------------------------------------------------------------------ geometry & layout

    private void computeGeometry() {
        ww = Math.min(width - 20, 440);
        wh = Math.min(height - 20, 280);
        wx = (width - ww) / 2;
        wy = (height - wh) / 2;
        float t = Math.min(1f, (System.currentTimeMillis() - openedAt) / 220f);
        float ease = 1f - (1f - t) * (1f - t) * (1f - t);
        alpha = Math.max(0.06f, ease);
        wy += (int) ((1f - ease) * 10);
        cx = wx + SIDE + 12;
        cw = ww - SIDE - 24;
        cy = wy + 44;
        ch = wh - 44 - 18;
    }

    private boolean friendsTab() { return category.equals(FRIENDS) && query.isEmpty(); }

    private List<Module> visibleModules() {
        List<Module> out = new ArrayList<>();
        String q = query.trim().toLowerCase(Locale.ROOT);
        for (Module m : ModuleManager.all()) {
            if (!q.isEmpty()) {
                if (m.name.toLowerCase(Locale.ROOT).contains(q) || m.description.toLowerCase(Locale.ROOT).contains(q)) out.add(m);
            } else if (m.category.equals(category)) {
                out.add(m);
            }
        }
        return out;
    }

    private static String selfName() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.player != null ? mc.player.getName().getString() : "";
    }

    private static List<String> onlineNames() {
        MinecraftClient mc = MinecraftClient.getInstance();
        List<String> out = new ArrayList<>();
        if (mc.getNetworkHandler() == null) return out;
        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) out.add(e.getProfile().getName());
        Collections.sort(out, String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    private void buildItems() {
        items.clear();
        if (friendsTab()) {
            buildFriendItems();
        } else if (settingsOf == null) {
            int gap = 6, w2 = (cw - gap) / 2, h = 30;
            List<Module> mods = visibleModules();
            for (int i = 0; i < mods.size(); i++) {
                int col = i % 2, row = i / 2;
                items.add(new Item(Kind.CARD, mods.get(i), null, 0, "", col * (w2 + gap), row * (h + gap), w2, h));
            }
            contentHeight = ((mods.size() + 1) / 2) * (h + gap);
        } else {
            Module m = settingsOf;
            int y = 0;
            items.add(new Item(Kind.TOGGLE_ROW, m, null, 0, "", 0, y, cw, 22));
            y += 26;
            if (m.hasActionKey) {
                items.add(new Item(Kind.KEY, m, null, 1, "", 0, y, cw, 22));
                y += 26;
            }
            if (m.hasActionKey2) {
                items.add(new Item(Kind.KEY, m, null, 2, "", 0, y, cw, 22));
                y += 26;
            }
            for (Setting s : m.settings) {
                if (s.type == Setting.Type.BOOL) {
                    items.add(new Item(Kind.BOOL, m, s, 0, "", 0, y, cw, 22));
                    y += 26;
                } else {
                    items.add(new Item(Kind.SLIDER, m, s, 0, "", 0, y, cw, 30));
                    y += 34;
                }
            }
            contentHeight = y;
        }
        scroll = Math.max(0, Math.min(scroll, Math.max(0, contentHeight - ch)));
    }

    private void buildFriendItems() {
        int y = 0;
        items.add(new Item(Kind.PROTECT, ModuleManager.FRIENDSAVE, null, 0, "", 0, y, cw, 22));
        y += 28;
        items.add(new Item(Kind.INPUT, null, null, 0, "", 0, y, cw, 22));
        y += 28;

        List<String> friends = new ArrayList<>(FriendManager.all());
        items.add(new Item(Kind.SECTION, null, null, 0, "Друзья (" + friends.size() + ")", 0, y, cw, 14));
        y += 17;
        if (friends.isEmpty()) {
            items.add(new Item(Kind.HINT, null, null, 0, "Пока никого. Введи ник выше или выбери игрока ниже.", 0, y, cw, 14));
            y += 18;
        }
        for (String f : friends) {
            items.add(new Item(Kind.FRIEND, null, null, 0, f, 0, y, cw, 22));
            y += 26;
        }
        y += 4;

        items.add(new Item(Kind.SECTION, null, null, 0, "Игроки на сервере (клик - добавить)", 0, y, cw, 14));
        y += 17;
        boolean connected = MinecraftClient.getInstance().getNetworkHandler() != null;
        String me = selfName();
        int shown = 0;
        for (String n : onlineNames()) {
            if (n.equalsIgnoreCase(me) || FriendManager.isFriend(n)) continue;
            if (shown++ >= 60) break;
            items.add(new Item(Kind.PLAYER, null, null, 0, n, 0, y, cw, 22));
            y += 26;
        }
        if (shown == 0) {
            items.add(new Item(Kind.HINT, null, null, 0,
                    connected ? "Других игроков не видно." : "Зайди на сервер, чтобы выбрать игрока из списка.", 0, y, cw, 14));
            y += 18;
        }
        contentHeight = y;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private boolean inContent(double mx, double my) { return inside(mx, my, cx, cy, cw, ch); }

    private int tabY(int i) { return wy + 46 + i * 22; }

    private static boolean expandable(Module m) {
        return !m.settings.isEmpty() || m.hasActionKey || m.hasActionKey2;
    }

    private void flash(String s) {
        status = s;
        statusUntil = System.currentTimeMillis() + 2500;
    }

    private boolean addFriend(String name) {
        if (!name.matches("[A-Za-z0-9_]{1,16}")) {
            flash("Ник: 1-16 символов, буквы, цифры и _");
            return false;
        }
        if (FriendManager.add(name)) {
            flash("Друг добавлен: " + name);
            ModuleManager.ISLAND.push("Друг добавлен: " + name);
            return true;
        }
        flash(name + " уже в списке");
        return false;
    }

    // ------------------------------------------------------------------ drawing helpers

    private int fade(int argb) {
        int a = (int) ((argb >>> 24) * alpha);
        return (a << 24) | (argb & 0xFFFFFF);
    }

    private static int lerp(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (((c1 >>> 24) & 255) * (1 - t) + ((c2 >>> 24) & 255) * t);
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void rr(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        RenderUtil.roundedRect(ctx, x, y, w, h, r, fade(color));
    }

    private void text(DrawContext ctx, String s, int x, int y, int color) {
        ctx.drawTextWithShadow(textRenderer, s, x, y, fade(color));
    }

    private void textScaled(DrawContext ctx, String s, int x, int y, int color, float scale) {
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate((float) x, (float) y, 0f);
        m.scale(scale, scale, 1f);
        ctx.drawTextWithShadow(textRenderer, s, 0, 0, fade(color));
        m.pop();
    }

    private String trim(String s, int maxW) {
        if (textRenderer.getWidth(s) <= maxW) return s;
        while (s.length() > 1 && textRenderer.getWidth(s + "..") > maxW) s = s.substring(0, s.length() - 1);
        return s + "..";
    }

    private void drawSwitch(DrawContext ctx, int x, int y, float anim) {
        rr(ctx, x, y, 22, 10, 5, lerp(0xFF3A3A4E, ACCENT, anim));
        rr(ctx, x + 1 + Math.round(anim * 12), y + 1, 8, 8, 4, 0xFFFFFFFF);
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // No super.render(): avoids double background blur; we draw our own dim layer.
        computeGeometry();
        buildItems();

        for (Module m : ModuleManager.all()) m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.3f;
        if (settingsOf != null) {
            for (Setting s : settingsOf.settings) {
                if (s.type == Setting.Type.BOOL) s.anim += ((s.asBool() ? 1f : 0f) - s.anim) * 0.3f;
            }
        }

        ctx.fill(0, 0, width, height, fade(0x88000000));

        for (Module m : ModuleManager.all()) {
            if (m.enabled && m.movable && m.boxW > 0) {
                ctx.drawBorder(m.boxX - 2, m.boxY - 2, m.boxW + 4, m.boxH + 4, ACCENT);
            }
        }

        // window with soft shadow and glowing edge
        rr(ctx, wx - 6, wy - 6, ww + 12, wh + 12, 13, 0x16000000);
        rr(ctx, wx - 3, wy - 3, ww + 6, wh + 6, 11, 0x24000000);
        rr(ctx, wx - 1, wy - 1, ww + 2, wh + 2, 9, 0x60B06CFF);
        rr(ctx, wx, wy, ww, wh, 8, 0xF0121220);
        // sidebar
        rr(ctx, wx, wy, SIDE, wh, 8, 0xF40C0C16);
        ctx.fill(wx + SIDE - 8, wy, wx + SIDE, wy + wh, fade(0xF40C0C16));
        ctx.fill(wx + SIDE, wy + 8, wx + SIDE + 1, wy + wh - 8, fade(0x30FFFFFF));

        textScaled(ctx, "Cold", wx + 13, wy + 11, 0xFF5C7CFF, 1.6f);
        textScaled(ctx, "Cold", wx + 12, wy + 10, ACCENT, 1.6f);
        text(ctx, "visuals beta", wx + 12, wy + 28, TEXT_DIM);

        for (int i = 0; i < TABS.length; i++) {
            int y = tabY(i);
            boolean sel = TABS[i].equals(category) && query.isEmpty();
            boolean hover = inside(mouseX, mouseY, wx + 6, y, SIDE - 12, 20);
            if (sel) {
                rr(ctx, wx + 6, y, SIDE - 12, 20, 6, 0x48B06CFF);
                rr(ctx, wx + 6, y + 4, 2, 12, 1, TAB_COLORS[i]);
            } else if (hover) {
                rr(ctx, wx + 6, y, SIDE - 12, 20, 6, 0x20FFFFFF);
            }
            rr(ctx, wx + 14, y + 6, 8, 8, 3, sel || hover ? TAB_COLORS[i] : lerp(TAB_COLORS[i], 0xFF2A2A3A, 0.55f));
            text(ctx, TABS[i], wx + 27, y + 6, sel ? TEXT : (hover ? 0xFFD0D0E0 : TEXT_DIM));

            int on = 0, total = 0;
            if (TABS[i].equals(FRIENDS)) {
                total = FriendManager.all().size();
                on = total;
            } else {
                for (Module m : ModuleManager.all()) {
                    if (m.category.equals(TABS[i])) {
                        total++;
                        if (m.enabled) on++;
                    }
                }
            }
            String cnt = TABS[i].equals(FRIENDS) ? String.valueOf(total) : on + "/" + total;
            text(ctx, cnt, wx + SIDE - 12 - textRenderer.getWidth(cnt), y + 6, sel ? 0xFFD9C2FF : 0xFF5F5F78);
        }

        // header
        if (settingsOf != null && !friendsTab()) {
            boolean hb = inside(mouseX, mouseY, cx, wy + 10, 56, 16);
            rr(ctx, cx, wy + 10, 56, 16, 6, hb ? 0xFF2C2C44 : 0xFF1F1F32);
            text(ctx, "< Назад", cx + 8, wy + 14, TEXT);
            text(ctx, settingsOf.name, cx + 66, wy + 10, TEXT);
            text(ctx, trim(settingsOf.description, cw - 70), cx + 66, wy + 21, TEXT_DIM);
        } else if (friendsTab()) {
            textScaled(ctx, "Друзья", cx, wy + 12, TEXT, 1.4f);
        } else {
            String title = query.isEmpty() ? category : "Поиск";
            textScaled(ctx, title, cx, wy + 12, TEXT, 1.4f);
        }
        if (settingsOf == null && !friendsTab()) {
            int sx = wx + ww - 128, sy = wy + 10;
            rr(ctx, sx - 1, sy - 1, 118, 18, 6, searchFocused ? 0xFFB06CFF : 0x30FFFFFF);
            rr(ctx, sx, sy, 116, 16, 5, 0xFF191926);
            boolean empty = query.isEmpty() && !searchFocused;
            String shown = empty ? "Поиск..." : query + (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
            text(ctx, trim(shown, 106), sx + 6, sy + 4, empty ? 0xFF5F5F78 : TEXT);
        }

        // content
        Set<String> online = new HashSet<>();
        for (String n : onlineNames()) online.add(n.toLowerCase(Locale.ROOT));

        ctx.enableScissor(cx - 2, cy, cx + cw + 2, cy + ch);
        for (Item it : items) {
            int ix = cx + it.x(), iy = cy + it.y() - scroll;
            if (iy + it.h() < cy || iy > cy + ch) continue;
            boolean hover = inContent(mouseX, mouseY) && inside(mouseX, mouseY, ix, iy, it.w(), it.h());
            switch (it.kind()) {
                case CARD -> drawCard(ctx, it, ix, iy, hover);
                case TOGGLE_ROW -> {
                    rr(ctx, ix, iy, it.w(), it.h(), 6, hover ? 0xFF222236 : PANEL);
                    text(ctx, "Включён", ix + 8, iy + 7, TEXT);
                    drawSwitch(ctx, ix + it.w() - 32, iy + 6, it.m().anim);
                }
                case BOOL -> {
                    rr(ctx, ix, iy, it.w(), it.h(), 6, hover ? 0xFF222236 : PANEL);
                    text(ctx, it.s().name, ix + 8, iy + 7, 0xFFE0E0EE);
                    drawSwitch(ctx, ix + it.w() - 32, iy + 6, it.s().anim);
                }
                case SLIDER -> drawSlider(ctx, it, ix, iy, hover);
                case KEY -> {
                    Module m = it.m();
                    int code = it.idx() == 1 ? m.actionKey : m.actionKey2;
                    String label = it.idx() == 1 ? m.actionLabel : m.action2Label;
                    boolean waiting = listening == m && listeningIdx == it.idx();
                    rr(ctx, ix, iy, it.w(), it.h(), 6, hover ? 0xFF222236 : PANEL);
                    text(ctx, label, ix + 8, iy + 7, 0xFFE0E0EE);
                    String v = waiting ? "нажми клавишу..." : (code >= 0 ? BindManager.keyName(code) : "нет");
                    int vw = textRenderer.getWidth(v) + 12;
                    rr(ctx, ix + it.w() - vw - 6, iy + 3, vw, 16, 5, waiting ? 0xFF4A3F10 : 0xFF2A2A42);
                    text(ctx, v, ix + it.w() - vw, iy + 7, waiting ? 0xFFFFE066 : 0xFF8FD3FF);
                }
                case PROTECT -> {
                    rr(ctx, ix, iy, it.w(), it.h(), 6, hover ? 0xFF222236 : PANEL);
                    text(ctx, "Защита: не бить друзей", ix + 8, iy + 7, TEXT);
                    drawSwitch(ctx, ix + it.w() - 32, iy + 6, ModuleManager.FRIENDSAVE.anim);
                }
                case INPUT -> {
                    int bw = it.w() - 64;
                    rr(ctx, ix - 1, iy - 1, bw + 2, it.h() + 2, 6, friendFocused ? 0xFFB06CFF : 0x30FFFFFF);
                    rr(ctx, ix, iy, bw, it.h(), 5, 0xFF191926);
                    boolean empty = friendInput.isEmpty() && !friendFocused;
                    String shown = empty ? "Ник друга..." : friendInput
                            + (friendFocused && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
                    text(ctx, shown, ix + 7, iy + 7, empty ? 0xFF5F5F78 : TEXT);
                    boolean hb = hover && mouseX >= ix + it.w() - 58;
                    rr(ctx, ix + it.w() - 58, iy, 58, it.h(), 5, hb ? 0xFFC58BFF : ACCENT);
                    text(ctx, "Добавить", ix + it.w() - 58 + (58 - textRenderer.getWidth("Добавить")) / 2, iy + 7, 0xFF16101F);
                }
                case SECTION -> {
                    text(ctx, it.label(), ix + 2, iy + 3, 0xFFB9A3E8);
                    ctx.fill(ix + textRenderer.getWidth(it.label()) + 8, iy + 7, ix + it.w(), iy + 8, fade(0x25FFFFFF));
                }
                case HINT -> text(ctx, trim(it.label(), it.w()), ix + 2, iy + 3, 0xFF6C6C86);
                case FRIEND -> {
                    boolean isOn = online.contains(it.label().toLowerCase(Locale.ROOT));
                    rr(ctx, ix, iy, it.w(), it.h(), 6, hover ? 0xFF222236 : PANEL);
                    rr(ctx, ix + 8, iy + 8, 6, 6, 3, isOn ? 0xFF66FF66 : 0xFF555566);
                    text(ctx, it.label(), ix + 20, iy + 7, isOn ? TEXT : 0xFFA8A8BC);
                    String st = isOn ? "в сети" : "не в сети";
                    text(ctx, st, ix + it.w() - 34 - textRenderer.getWidth(st), iy + 7, isOn ? 0xFF66E08A : 0xFF6C6C86);
                    boolean hx = hover && mouseX >= ix + it.w() - 26;
                    rr(ctx, ix + it.w() - 24, iy + 3, 16, 16, 5, hx ? 0xFF8A2A3A : 0xFF2A2A42);
                    text(ctx, "x", ix + it.w() - 24 + 5, iy + 7, hx ? 0xFFFFFFFF : 0xFFB0B0C8);
                }
                case PLAYER -> {
                    rr(ctx, ix, iy, it.w(), it.h(), 6, hover ? 0xFF262640 : PANEL);
                    rr(ctx, ix + 8, iy + 8, 6, 6, 3, 0xFF66E08A);
                    text(ctx, it.label(), ix + 20, iy + 7, TEXT);
                    String add = "+ в друзья";
                    int aw = textRenderer.getWidth(add) + 12;
                    rr(ctx, ix + it.w() - aw - 6, iy + 3, aw, 16, 5, hover ? ACCENT : 0xFF2E2848);
                    text(ctx, add, ix + it.w() - aw, iy + 7, hover ? 0xFF16101F : 0xFFD9C2FF);
                }
            }
        }
        ctx.disableScissor();

        // scrollbar
        if (contentHeight > ch) {
            int trackH = ch - 4;
            int barH = Math.max(16, trackH * ch / contentHeight);
            int barY = cy + 2 + (trackH - barH) * scroll / Math.max(1, contentHeight - ch);
            rr(ctx, cx + cw + 4, cy + 2, 2, trackH, 1, 0x20FFFFFF);
            rr(ctx, cx + cw + 4, barY, 2, barH, 1, 0xB0B06CFF);
        }

        // footer
        if (System.currentTimeMillis() < statusUntil) {
            text(ctx, trim(status, cw), cx, wy + wh - 13, 0xFF66E08A);
        } else {
            String hint = friendsTab()
                    ? "Клик по игроку - добавить   x - убрать   Enter в поле ника"
                    : (settingsOf == null
                    ? "ЛКМ вкл/выкл   ПКМ настройки   колесо прокрутка   Right Shift закрыть"
                    : "Esc назад   ПКМ по клавише - сбросить");
            text(ctx, trim(hint, cw), cx, wy + wh - 13, 0xFF5F5F78);
        }
    }

    private void drawCard(DrawContext ctx, Item it, int ix, int iy, boolean hover) {
        Module m = it.m();
        float h = hoverAnim.getOrDefault(m, 0f);
        h += ((hover ? 1f : 0f) - h) * 0.3f;
        hoverAnim.put(m, h);

        int base = lerp(PANEL, PANEL_ON, m.anim);
        base = lerp(base, 0xFFFFFFFF, 0.08f * h);
        rr(ctx, ix, iy, it.w(), it.h(), 6, base);
        if (m.anim > 0.05f) rr(ctx, ix, iy + 6, 2, it.h() - 12, 1, ACCENT);

        text(ctx, trim(m.name, it.w() - 62), ix + 9, iy + 5, lerp(0xFFC8C8D8, TEXT, m.anim));
        text(ctx, trim(m.description, it.w() - 62), ix + 9, iy + 17, TEXT_DIM);

        if (expandable(m)) {
            for (int k = 0; k < 3; k++) {
                rr(ctx, ix + it.w() - 48 + k * 4, iy + it.h() / 2 - 1, 2, 2, 1, hover ? 0xFFC8C8E0 : 0xFF6C6C88);
            }
        }
        drawSwitch(ctx, ix + it.w() - 32, iy + (it.h() - 10) / 2, m.anim);
    }

    private void drawSlider(DrawContext ctx, Item it, int ix, int iy, boolean hover) {
        Setting s = it.s();
        rr(ctx, ix, iy, it.w(), it.h(), 6, hover || dragSetting == s ? 0xFF222236 : PANEL);
        text(ctx, s.name, ix + 8, iy + 5, 0xFFE0E0EE);
        String v = String.valueOf(Math.round(s.value * 10) / 10.0);
        text(ctx, v, ix + it.w() - 8 - textRenderer.getWidth(v), iy + 5, 0xFF8FD3FF);

        int tx = ix + 8, tw = it.w() - 16, ty = iy + 20;
        double frac = (s.value - s.min) / (s.max - s.min);
        int fill = Math.max(4, (int) (tw * frac));
        rr(ctx, tx, ty, tw, 4, 2, 0xFF2C2C42);
        rr(ctx, tx, ty, fill, 4, 2, ACCENT);
        rr(ctx, tx + fill - 4, ty - 2, 8, 8, 4, 0xFFFFFFFF);
    }

    // ------------------------------------------------------------------ input

    private void applySlider(Setting s, double mouseX) {
        double frac = Math.max(0, Math.min(1, (mouseX - dragX) / Math.max(1, dragW)));
        s.value = Math.round((s.min + frac * (s.max - s.min)) * 10) / 10.0;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        computeGeometry();
        buildItems();
        listening = null; // any click cancels waiting for a key
        friendFocused = false;

        if (!inside(mx, my, wx, wy, ww, wh)) {
            searchFocused = false;
            if (button == 0) {
                var mods = ModuleManager.all();
                for (int i = mods.size() - 1; i >= 0; i--) {
                    Module m = mods.get(i);
                    if (m.enabled && m.hitBox(mx, my)) {
                        draggingHud = m;
                        return true;
                    }
                }
            }
            return super.mouseClicked(mx, my, button);
        }

        // search box
        if (settingsOf == null && !friendsTab() && inside(mx, my, wx + ww - 128, wy + 10, 116, 16)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        // back button
        if (settingsOf != null && !friendsTab() && inside(mx, my, cx, wy + 10, 56, 16)) {
            settingsOf = null;
            scroll = 0;
            return true;
        }

        // tabs
        for (int i = 0; i < TABS.length; i++) {
            if (inside(mx, my, wx + 6, tabY(i), SIDE - 12, 20)) {
                category = TABS[i];
                settingsOf = null;
                query = "";
                scroll = 0;
                return true;
            }
        }

        // content items
        if (inContent(mx, my)) {
            for (Item it : items) {
                int ix = cx + it.x(), iy = cy + it.y() - scroll;
                if (!inside(mx, my, ix, iy, it.w(), it.h())) continue;
                Module m = it.m();
                switch (it.kind()) {
                    case CARD -> {
                        boolean onDots = expandable(m) && mx >= ix + it.w() - 54 && mx < ix + it.w() - 36;
                        if (button == 1 || (button == 0 && onDots)) {
                            settingsOf = m;
                            scroll = 0;
                        } else if (button == 0) {
                            m.toggle();
                        }
                    }
                    case TOGGLE_ROW, PROTECT -> {
                        if (button == 0) m.toggle();
                    }
                    case BOOL -> {
                        if (button == 0) it.s().value = it.s().asBool() ? 0 : 1;
                    }
                    case SLIDER -> {
                        if (button == 0) {
                            dragSetting = it.s();
                            dragX = ix + 8;
                            dragW = it.w() - 16;
                            applySlider(dragSetting, mx);
                        }
                    }
                    case KEY -> {
                        if (button == 0) {
                            listening = m;
                            listeningIdx = it.idx();
                        } else if (button == 1) {
                            if (it.idx() == 1) m.actionKey = -1;
                            else m.actionKey2 = -1;
                        }
                    }
                    case INPUT -> {
                        if (button == 0) {
                            if (mx >= ix + it.w() - 58) {
                                if (addFriend(friendInput.trim())) friendInput = "";
                            } else {
                                friendFocused = true;
                            }
                        }
                    }
                    case FRIEND -> {
                        if (button == 0 && mx >= ix + it.w() - 26) {
                            FriendManager.remove(it.label());
                            flash("Друг удалён: " + it.label());
                        }
                    }
                    case PLAYER -> {
                        if (button == 0) addFriend(it.label());
                    }
                    case SECTION, HINT -> { }
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragSetting != null) {
            applySlider(dragSetting, mx);
            return true;
        }
        if (draggingHud != null) {
            draggingHud.moveBy(dx, dy);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragSetting = null;
        draggingHud = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontalAmount, double verticalAmount) {
        scroll -= (int) Math.round(verticalAmount * 16);
        if (scroll < 0) scroll = 0;
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (friendFocused) {
            boolean ok = (chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z') || (chr >= '0' && chr <= '9') || chr == '_';
            if (ok && friendInput.length() < 16) friendInput += chr;
            return true;
        }
        if (searchFocused && chr >= 32 && chr != 127 && query.length() < 24) {
            query += chr;
            scroll = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening != null) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                int code = (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) ? -1 : keyCode;
                if (listeningIdx == 1) listening.actionKey = code;
                else listening.actionKey2 = code;
            }
            listening = null;
            return true;
        }
        if (friendFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!friendInput.isEmpty()) friendInput = friendInput.substring(0, friendInput.length() - 1);
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                if (addFriend(friendInput.trim())) friendInput = "";
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                friendFocused = false;
            }
            return true;
        }
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!query.isEmpty()) query = query.substring(0, query.length() - 1);
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && settingsOf != null) {
            settingsOf = null;
            scroll = 0;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
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
