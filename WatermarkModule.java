package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;

import java.util.ArrayList;
import java.util.List;

public class WatermarkModule extends Module {
    /** Change this text to rename your visual. */
    public static final String NAME = "ColdVisuals-beta";

    public final Setting server = add(Setting.bool("Show Server", true));
    public final Setting ping = add(Setting.bool("Show Ping", true));
    public final Setting fps = add(Setting.bool("Show FPS", true));
    public final Setting r = add(Setting.number("Red", 170, 0, 255));
    public final Setting g = add(Setting.number("Green", 90, 0, 255));
    public final Setting b = add(Setting.number("Blue", 255, 0, 255));

    public WatermarkModule() {
        super("Watermark", "Floating pill with name, server, ping and FPS (draggable)");
        makeMovable(0, 4); // posX is an offset from the screen center
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        TextRenderer tr = mc.textRenderer;
        int accent = 0xFF000000 | (r.asInt() << 16) | (g.asInt() << 8) | b.asInt();

        List<String> parts = new ArrayList<>();
        if (server.asBool()) {
            ServerInfo info = mc.getCurrentServerEntry();
            parts.add(info != null ? info.address : "singleplayer");
        }
        if (ping.asBool() && mc.getNetworkHandler() != null) {
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (e != null) parts.add(e.getLatency() + " ms");
        }
        if (fps.asBool()) parts.add(mc.getCurrentFps() + " FPS");

        int pad = 8, dot = 6, sepGap = 6;
        int sepW = sepGap * 2 + tr.getWidth("|");
        int w = pad + dot + 4 + tr.getWidth(NAME) + pad;
        for (String p : parts) w += sepW + tr.getWidth(p);
        int h = 16;

        int base = (ctx.getScaledWindowWidth() - w) / 2;
        int x = (int) Math.max(0, Math.min(base + posX, ctx.getScaledWindowWidth() - w));
        posX = x - base;
        int yy = clampY(ctx, h);
        setBox(x, yy, w, h);

        RenderUtil.roundedRect(ctx, x, yy, w, h, 8, 0xB0101018);
        RenderUtil.roundedRect(ctx, x + pad, yy + 5, dot, dot, 3, accent);

        int cx = x + pad + dot + 4;
        ctx.drawTextWithShadow(tr, NAME, cx, yy + 4, 0xFFFFFFFF);
        cx += tr.getWidth(NAME);
        for (String p : parts) {
            ctx.drawTextWithShadow(tr, "|", cx + sepGap, yy + 4, 0xFF777777);
            cx += sepW;
            ctx.drawTextWithShadow(tr, p, cx, yy + 4, 0xFFDDDDDD);
            cx += tr.getWidth(p);
        }
    }
}
