package com.example.visuals.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

/** Converts a point in the world to GUI coordinates, so HUD code can draw labels "in" the world. */
public class Project {
    /** Vertical field of view in degrees; updated every frame by GameRendererMixin. */
    public static float fov = 70f;

    /** Returns {x, y} in scaled GUI pixels, or null when the point is behind the camera. */
    public static int[] toScreen(MinecraftClient mc, DrawContext ctx, Vec3d pos) {
        Camera cam = mc.gameRenderer.getCamera();
        Vec3d rel = pos.subtract(cam.getPos());
        double yaw = Math.toRadians(cam.getYaw());
        double pitch = Math.toRadians(cam.getPitch());

        double fx = -Math.sin(yaw) * Math.cos(pitch);
        double fy = -Math.sin(pitch);
        double fz = Math.cos(yaw) * Math.cos(pitch);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        double ux = -rz * fy, uy = rz * fx - rx * fz, uz = rx * fy;

        double zf = rel.x * fx + rel.y * fy + rel.z * fz;
        if (zf < 0.05) return null;
        double xr = rel.x * rx + rel.z * rz;
        double yu = rel.x * ux + rel.y * uy + rel.z * uz;

        double tanHalf = Math.tan(Math.toRadians(fov) / 2.0);
        double w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
        double ndcX = xr / (zf * tanHalf * (w / h));
        double ndcY = yu / (zf * tanHalf);
        return new int[]{(int) (w * (0.5 + ndcX * 0.5)), (int) (h * (0.5 - ndcY * 0.5))};
    }
}
