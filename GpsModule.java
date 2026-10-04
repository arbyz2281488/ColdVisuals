package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.Locale;

/** Waypoint panel: compass strip, direction arrow and distance in meters. Set with: .gps set X Z */
public class GpsModule extends Module {
    public final Setting compass = add(Setting.bool("Show Compass", true));
    public final Setting arrow = add(Setting.bool("Show Arrow", true));

    private static final int ACCENT = 0xFFB06CFF;

    private boolean hasTarget;
    private double tx, tz;

    public GpsModule() {
        super("GPS", "Waypoint: compass, arrow and distance in meters (.gps set X Z)");
        makeMovable(0, 26); // posX is an offset from the screen center
    }

    public void setTarget(double x, double z) {
        tx = x;
        tz = z;
        hasTarget = true;
        enabled = true;
    }

    public void off() {
        hasTarget = false;
        enabled = false;
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        if (!hasTarget) return;
        TextRenderer tr = mc.textRenderer;

        double dx = tx - mc.player.getX();
        double dz = tz - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = mc.player.getYaw();
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float rel = MathHelper.wrapDegrees(targetYaw - yaw); // >0 = target is to the right

        int w = 190;
        int h = 16 + (compass.asBool() ? 18 : 0) + 24;
        int base = (ctx.getScaledWindowWidth() - w) / 2;
        int x = (int) Math.max(0, Math.min(base + posX, ctx.getScaledWindowWidth() - w));
        posX = x - base;
        int yy = clampY(ctx, h);
        setBox(x, yy, w, h);

        RenderUtil.roundedRect(ctx, x, yy, w, h, 4, 0xB0101018);
        ctx.drawTextWithShadow(tr, "GPS", x + 6, yy + 4, ACCENT);
        String coords = "X " + Math.round(tx) + "  Z " + Math.round(tz);
        ctx.drawTextWithShadow(tr, coords, x + w - 6 - tr.getWidth(coords), yy + 4, 0xFFDDDDDD);

        int curY = yy + 16;

        if (compass.asBool()) {
            int sw = w - 10;
            int sx = x + 5;
            int cx = x + w / 2;
            ctx.fill(sx, curY, sx + sw, curY + 14, 0x40000000);

            float heading = MathHelper.wrapDegrees(yaw + 180f); // 0 = north
            for (int k = 0; k < 360; k += 15) {
                float r = MathHelper.wrapDegrees(k - heading);
                if (Math.abs(r) > 88) continue;
                int px = cx + Math.round(r / 90f * (sw / 2f));
                if (k % 90 == 0) {
                    String letter = k == 0 ? "N" : k == 90 ? "E" : k == 180 ? "S" : "W";
                    int col = k == 0 ? 0xFFFF5555 : 0xFFFFFFFF;
                    ctx.drawTextWithShadow(tr, letter, px - tr.getWidth(letter) / 2, curY + 3, col);
                } else {
                    ctx.fill(px, curY + 8, px + 1, curY + 13, 0x88FFFFFF);
                }
            }

            if (Math.abs(rel) <= 88) {
                int mx = cx + Math.round(rel / 90f * (sw / 2f));
                ctx.fill(mx - 1, curY, mx + 2, curY + 14, ACCENT);
            } else if (rel > 0) {
                ctx.drawTextWithShadow(tr, ">", sx + sw - 9, curY + 3, ACCENT);
            } else {
                ctx.drawTextWithShadow(tr, "<", sx + 3, curY + 3, ACCENT);
            }
            curY += 18;
        }

        int textX = x + 8;
        if (arrow.asBool()) {
            MatrixStack m = ctx.getMatrices();
            m.push();
            m.translate((float) (x + 22), (float) (curY + 11), 0f);
            m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rel));
            for (int i = 0; i < 6; i++) {
                ctx.fill(-i, -8 + i, i + 1, -7 + i, ACCENT); // arrow head
            }
            ctx.fill(-1, -2, 2, 7, ACCENT);                   // shaft
            m.pop();
            textX = x + 44;
        }

        String d;
        if (dist < 3) d = "На месте";
        else if (dist >= 1000) d = String.format(Locale.ROOT, "%.1f км", dist / 1000.0);
        else d = Math.round(dist) + " м";

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate((float) textX, (float) (curY + 4), 0f);
        m.scale(1.5f, 1.5f, 1f);
        ctx.drawTextWithShadow(tr, d, 0, 0, 0xFFFFFFFF);
        m.pop();
    }
}
