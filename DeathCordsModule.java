package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Locale;

/** Prints your death coordinates to your own chat window (only you see it) and on the Island. */
public class DeathCordsModule extends Module {
    private boolean wasDead;

    public DeathCordsModule() { super("DeathCords", "Shows your death coordinates in chat"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        boolean dead = mc.player.isDead();
        if (enabled && dead && !wasDead) {
            String dim = mc.world.getRegistryKey().getValue().getPath();
            int x = mc.player.getBlockX(), y = mc.player.getBlockY(), z = mc.player.getBlockZ();
            mc.player.sendMessage(Text.literal(String.format(Locale.ROOT,
                    "§c[Смерть] §fX %d  Y %d  Z %d  (%s)", x, y, z, dim)), false);
            ModuleManager.ISLAND.push(String.format(Locale.ROOT, "Смерть: %d %d %d", x, y, z));
        }
        wasDead = dead;
    }
}
