package com.example.visuals;

import com.example.visuals.modules.ModuleManager;
import com.example.visuals.modules.ShoulderModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Draws the shoulder models. The meshes were flattened from the Blockbench files when the mod was built
 * (24 numbers per quad: texture, 4 x [x y z u v], normal) and are drawn straight through the vertex consumer.
 */
public final class ShoulderRenderer {
    /** Wanted height in blocks at 100 % scale (the sword is shown larger). */
    private static final float[] TARGET = {0.45f, 0.55f, 1.2f};

    private static final Map<Integer, float[]> MESH = new HashMap<>();
    private static final Map<String, Identifier[]> TEX = new HashMap<>();
    private static boolean failed;

    private ShoulderRenderer() {}

    private static float[] mesh(int idx) {
        float[] m = MESH.get(idx);
        if (m != null) return m;
        ByteBuffer bb = ByteBuffer.wrap(Base64.getDecoder().decode(String.join("", ShoulderModelData.MESH[idx])));
        int n = bb.getInt();
        float[] f = new float[n * 24];
        for (int i = 0; i < f.length; i++) f[i] = bb.getFloat();
        MESH.put(idx, f);
        return f;
    }

    private static Identifier[] textures(int idx, int variant) throws IOException {
        String key = idx + ":" + variant;
        Identifier[] ids = TEX.get(key);
        if (ids != null) return ids;
        String[][] set = ShoulderModelData.TEX[idx];
        ids = new Identifier[set.length];
        for (int j = 0; j < set.length; j++) {
            byte[] png = Base64.getDecoder().decode(String.join("", set[j]));
            if (j != ShoulderModelData.EMISSIVE[idx]) png = Recolor.apply(png, variant);
            NativeImage img = NativeImage.read(new ByteArrayInputStream(png));
            ids[j] = Identifier.of("visuals", "shoulder_" + idx + "_v" + variant + "_" + j);
            MinecraftClient.getInstance().getTextureManager().registerTexture(ids[j], new NativeImageBackedTexture(img));
        }
        TEX.put(key, ids);
        return ids;
    }

    public static void render(PlayerEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vcp, int light) {
        ShoulderModule m = ModuleManager.SHOULDER;
        if (!m.enabled || failed) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // only for yourself: the player being drawn must be at your position
        double dx = state.x - mc.player.getX(), dz = state.z - mc.player.getZ(), dy = state.y - mc.player.getY();
        if (dx * dx + dz * dz > 0.5 || Math.abs(dy) > 1.0) return;

        try {
            int idx = Math.max(0, Math.min(2, m.model.asInt() - 1));
            int variant = Math.max(0, Math.min(Recolor.COUNT - 1, m.texture.asInt()));
            float[] quads = mesh(idx);
            Identifier[] ids = textures(idx, variant);

            float s = TARGET[idx] * (float) (m.scale.value / 100.0) / ShoulderModelData.HEIGHT[idx];
            float sneak = state.sneaking ? -0.25f : 0f;
            float lift = (float) m.height.value;

            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - state.bodyYaw));
            switch (m.place.asInt()) {
                case 2 -> matrices.translate(-0.40f, 1.38f + sneak + lift, 0f);
                case 3 -> {
                    matrices.translate(0f, 0.95f + sneak + lift, 0.28f);
                    if (idx == 2) matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45f));
                }
                default -> matrices.translate(0.40f, 1.38f + sneak + lift, 0f);
            }
            matrices.scale(s, s, s);
            draw(quads, ids, idx, matrices, vcp, light);
            matrices.pop();
        } catch (Throwable t) {
            failed = true; // do not spam the game with the same error
            ErrorLog.write("ShoulderRenderer", t);
        }
    }

    private static void draw(float[] q, Identifier[] ids, int idx, MatrixStack matrices, VertexConsumerProvider vcp, int light) {
        MatrixStack.Entry e = matrices.peek();
        int glow = ShoulderModelData.EMISSIVE[idx];
        for (int j = 0; j < ids.length; j++) {
            if (j == glow) continue;
            emit(vcp.getBuffer(RenderLayer.getEntityCutoutNoCull(ids[j])), q, j, e, light);
        }
        if (glow >= 0 && glow < ids.length) {
            emit(vcp.getBuffer(RenderLayer.getEyes(ids[glow])), q, 0, e, 0xF000F0);
        }
    }

    private static void emit(VertexConsumer vc, float[] q, int tex, MatrixStack.Entry e, int light) {
        for (int i = 0; i + 23 < q.length; i += 24) {
            if ((int) q[i] != tex) continue;
            float nx = q[i + 21], ny = q[i + 22], nz = q[i + 23];
            for (int v = 0; v < 4; v++) {
                int o = i + 1 + v * 5;
                vc.vertex(e, q[o], q[o + 1], q[o + 2])
                        .color(255, 255, 255, 255)
                        .texture(q[o + 3], q[o + 4])
                        .overlay(OverlayTexture.DEFAULT_UV)
                        .light(light)
                        .normal(e, nx, ny, nz);
            }
        }
    }
}
