package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * "Dynamic island": a pill at the top that shows clock / FPS / ping, and smoothly widens to show short
 * notifications from other modules (friend joined, mention, death coordinates).
 */
public class IslandModule extends Module {
    public final Setting clock = add(Setting.bool("Show Clock", true));
    public final Setting fps = add(Setting.bool("Show FPS", true));
    public final Setting ping = add(Setting.bool("Show Ping", false));

    private record Note(String text, long until) {}

    private final Deque<Note> notes = new ArrayDeque<>();
    private float curW = 80;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    public IslandModule() {
        super("Island", "Dynamic island with clock and notifications (draggable)");
        makeMovable(0, 24); // posX is an offset from the screen center
    }

    /** Shows a short notification (3.5 s). Does nothing while the module is off. */
    public void push(String text) {
        if (!enabled) return;
        notes.addLast(new Note(text, System.currentTimeMillis() + 3500));
        while (notes.size() > 4) notes.removeFirst();
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        TextRenderer tr = mc.textRenderer;
        long now = System.currentTimeMillis();
        while (!notes.isEmpty() && notes.peekFirst().until() < now) notes.removeFirst();

        boolean alert = !notes.isEmpty();
        String text;
        if (alert) {
            text = notes.peekFirst().text();
        } else {
            List<String> parts = new ArrayList<>();
            if (clock.asBool()) parts.add(LocalTime.now().format(TIME));
            if (fps.asBool()) parts.add(mc.getCurrentFps() + " FPS");
            if (ping.asBool() && mc.getNetworkHandler() != null) {
                var e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (e != null) parts.add(e.getLatency() + " ms");
            }
            text = parts.isEmpty() ? "Cold" : String.join("  |  ", parts);
        }

        int target = tr.getWidth(text) + 28;
        curW += (target - curW) * 0.25f;
        int w = Math.round(curW), h = 18;

        int base = (ctx.getScaledWindowWidth() - w) / 2;
        int x = (int) Math.max(0, Math.min(base + posX, ctx.getScaledWindowWidth() - w));
        posX = x - base;
        int yy = clampY(ctx, h);
        setBox(x, yy, w, h);

        RenderUtil.roundedRect(ctx, x - 1, yy - 1, w + 2, h + 2, 10, alert ? 0xFFB06CFF : 0x66B06CFF);
        RenderUtil.roundedRect(ctx, x, yy, w, h, 9, 0xF0101018);
        RenderUtil.roundedRect(ctx, x + 8, yy + 6, 6, 6, 3, 0xFFB06CFF);
        ctx.drawTextWithShadow(tr, text, x + 20, yy + 5, 0xFFFFFFFF);
    }
}
