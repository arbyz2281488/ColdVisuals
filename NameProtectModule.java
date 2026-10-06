package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Optional;

/** Replaces your nickname with a fake one in incoming chat messages (for screenshots and streams). */
public class NameProtectModule extends Module {
    /** The name shown instead of yours. Change it here. */
    public static final String FAKE_NAME = "ColdPlayer";

    public NameProtectModule() { super("NameProtect", "Hides your real nickname in chat"); }

    public Text apply(Text message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!enabled || mc.player == null) return message;
        String me = mc.player.getName().getString();
        if (me.isEmpty() || !message.getString().contains(me)) return message;

        MutableText out = Text.empty();
        message.visit((style, str) -> {
            out.append(Text.literal(str.replace(me, FAKE_NAME)).setStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }
}
