package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public boolean enabled;
    public final List<Setting> settings = new ArrayList<>();

    protected Module(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected Setting add(Setting s) {
        settings.add(s);
        return s;
    }

    public void toggle() { enabled = !enabled; }

    /** Called every tick, even when disabled (so effects can smoothly unwind). */
    public void onTick(MinecraftClient mc) {}

    /** Called every frame for HUD drawing, only when enabled. */
    public void onHud(DrawContext ctx, MinecraftClient mc) {}
}
