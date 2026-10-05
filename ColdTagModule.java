package com.example.visuals.modules;

import com.example.visuals.ColdNet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Shows a [Cold] tag next to the nickname of players who use these visuals (you included). */
public class ColdTagModule extends Module {
    public final Setting nametag = add(Setting.bool("Nametag", true));
    public final Setting tab = add(Setting.bool("Tab List", true));
    public final Setting network = add(Setting.bool("Find Other Users", true));

    public ColdTagModule() { super("ColdTag", "Shows [Cold] next to nicknames of Cold users"); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || !network.asBool()) return;
        ColdNet.tick(mc);
    }

    public boolean isColdUser(String name) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.getName().getString().equalsIgnoreCase(name)) return true;
        return ColdNet.isUser(name);
    }

    public static Text decorate(Text original) {
        return Text.literal("")
                .append(Text.literal("[Cold] ").formatted(Formatting.AQUA))
                .append(original);
    }
}
