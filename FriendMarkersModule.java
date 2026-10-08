package com.example.visuals.modules;

import com.example.visuals.FriendManager;
import com.example.visuals.gui.Project;
import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

/**
 * Green marker (with distance and worn armor) over your friends' heads. Only shown while you can actually
 * see the friend, so it is a highlight and not an X-ray. Manage friends in the GUI (Friends tab) or with .friend
 */
public class FriendMarkersModule extends Module {
    public final Setting armor = add(Setting.bool("Show Armor", true));
    public final Setting distance = add(Setting.bool("Show Distance", true));

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public FriendMarkersModule() { super("FriendMarkers", "Marks your friends over their heads"); }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !FriendManager.isFriend(p.getName().getString())) continue;
            float d = mc.player.distanceTo(p);
            if (d > 80 || !mc.player.canSee(p)) continue;

            int[] xy = Project.toScreen(mc, ctx, new Vec3d(p.getX(), p.getY() + p.getHeight() + 0.75, p.getZ()));
            if (xy == null) continue;

            RenderUtil.roundedRect(ctx, xy[0] - 4, xy[1] - 4, 8, 8, 4, 0xFF66E08A);
            RenderUtil.roundedRect(ctx, xy[0] - 2, xy[1] - 2, 4, 4, 2, 0xFF0F2A18);
            if (distance.asBool()) {
                String t = Math.round(d) + " м";
                ctx.drawTextWithShadow(mc.textRenderer, t, xy[0] - mc.textRenderer.getWidth(t) / 2, xy[1] + 6, 0xFFB8F5CB);
            }
            if (armor.asBool()) {
                MatrixStack m = ctx.getMatrices();
                m.push();
                m.translate((float) xy[0], (float) xy[1] - 22f, 0f);
                m.scale(0.75f, 0.75f, 1f);
                int x = -32;
                for (EquipmentSlot slot : SLOTS) {
                    ItemStack st = p.getEquippedStack(slot);
                    if (!st.isEmpty()) ctx.drawItem(st, x, 0);
                    x += 16;
                }
                m.pop();
            }
        }
    }
}
