package com.example.visuals.modules;

import com.example.visuals.CommandBinds;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Sends one command or message every N seconds. Set it with: .autocmd /command */
public class AutoCommandModule extends Module {
    public final Setting interval = add(Setting.number("Interval s", 60, 5, 600));

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visuals-autocmd.txt");
    private String command;
    private int ticks;

    public AutoCommandModule() { super("AutoCommand", "Sends your command every N seconds (.autocmd /cmd)"); }

    public String command() {
        if (command == null) {
            command = "";
            try {
                if (Files.exists(FILE)) command = Files.readString(FILE).trim();
            } catch (IOException ignored) {}
        }
        return command;
    }

    public void setCommand(String c) {
        command = c == null ? "" : c.trim();
        try {
            Files.writeString(FILE, command);
        } catch (IOException ignored) {}
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.player == null || command().isEmpty()) {
            ticks = 0;
            return;
        }
        if (++ticks >= interval.asInt() * 20) {
            ticks = 0;
            CommandBinds.run(mc, command());
        }
    }
}
