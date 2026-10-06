package com.example.visuals.modules;

import com.example.visuals.FriendManager;
import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Friends list with online status (online = present in the TAB list). Manage with .friend */
public class FriendsModule extends Module {
    private record Line(String name, boolean online) {}

    public FriendsModule() {
        super("Friends", "Friend list with online status (draggable). Use .friend add <nick>");
        makeMovable(6, 440);
    }

    private Set<String> prev = null;

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.getNetworkHandler() == null || mc.player == null) {
            prev = null;
            return;
        }
        if (mc.player.age % 20 != 0) return;
        Set<String> now = new HashSet<>();
        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) {
            now.add(e.getProfile().getName().toLowerCase(Locale.ROOT));
        }
        if (prev != null) {
            for (String f : FriendManager.all()) {
                String k = f.toLowerCase(Locale.ROOT);
                boolean was = prev.contains(k), is = now.contains(k);
                if (is && !was) ModuleManager.ISLAND.push("Друг в сети: " + f);
                else if (!is && was) ModuleManager.ISLAND.push("Друг вышел: " + f);
            }
        }
        prev = now;
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        Set<String> friends = FriendManager.all();
        if (friends.isEmpty()) return;

        Set<String> online = new HashSet<>();
        if (mc.getNetworkHandler() != null) {
            for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) {
                online.add(e.getProfile().getName().toLowerCase(Locale.ROOT));
            }
        }

        List<Line> on = new ArrayList<>();
        List<Line> off = new ArrayList<>();
        for (String f : friends) {
            if (online.contains(f.toLowerCase(Locale.ROOT))) on.add(new Line(f, true));
            else off.add(new Line(f, false));
        }
        List<Line> lines = new ArrayList<>(on);
        lines.addAll(off);

        TextRenderer tr = mc.textRenderer;
        String title = "Друзья " + on.size() + "/" + friends.size();
        int w = tr.getWidth(title) + 20;
        for (Line l : lines) w = Math.max(w, tr.getWidth(l.name()) + 24);
        w = Math.max(w, 90);
        int rowH = 11;
        int h = (lines.size() + 1) * rowH + 8;
        int px = clampX(ctx, w), py = clampY(ctx, h);
        setBox(px, py, w, h);

        RenderUtil.roundedRect(ctx, px, py, w, h, 4, 0xB0101018);
        ctx.drawTextWithShadow(tr, title, px + 6, py + 5, 0xFFB06CFF);
        int ty = py + 5 + rowH;
        for (Line l : lines) {
            ctx.drawTextWithShadow(tr, "• " + l.name(), px + 6, ty, l.online() ? 0xFF66FF66 : 0xFF777777);
            ty += rowH;
        }
    }
}
