package com.example.visuals;

import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Replaces the game window icon (and so the taskbar icon) with the ice "C". Done once, on the first tick. */
public class WindowIcon {
    private static boolean done;

    public static void apply(MinecraftClient mc) {
        if (done || mc.getWindow() == null) return;
        done = true;
        if (!ModuleManager.MENUSTYLE.windowIcon.asBool()) return;

        GLFWImage.Buffer images = null;
        List<ByteBuffer> pixels = new ArrayList<>();
        try {
            int n = WindowIconData.ICONS.length;
            images = GLFWImage.malloc(n);
            for (int i = 0; i < n; i++) {
                byte[] png = Base64.getDecoder().decode(String.join("", WindowIconData.ICONS[i]));
                BufferedImage bi = ImageIO.read(new ByteArrayInputStream(png));
                int w = bi.getWidth(), h = bi.getHeight();
                int[] argb = bi.getRGB(0, 0, w, h, null, 0, w);
                ByteBuffer bb = MemoryUtil.memAlloc(w * h * 4);
                for (int p : argb) {
                    bb.put((byte) (p >> 16)).put((byte) (p >> 8)).put((byte) p).put((byte) (p >>> 24));
                }
                bb.flip();
                images.get(i).set(w, h, bb);
                pixels.add(bb);
            }
            GLFW.glfwSetWindowIcon(mc.getWindow().getHandle(), images);
        } catch (Throwable ignored) {
            // keep the default icon if anything goes wrong
        } finally {
            if (images != null) images.free();
            for (ByteBuffer b : pixels) MemoryUtil.memFree(b);
        }
    }
}
