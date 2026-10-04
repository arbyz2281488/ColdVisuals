package com.example.visuals.modules;

import com.example.visuals.VisualsClient;
import net.minecraft.client.MinecraftClient;

public class ZoomModule extends Module {
    public final Setting strength = add(Setting.number("Strength", 4.0, 1.5, 10.0));
    public final Setting smooth = add(Setting.bool("Smooth", true));
    private float current = 1f;

    public ZoomModule() { super("Zoom", "Hold the zoom key (C) to zoom in"); }

    @Override
    public void onTick(MinecraftClient mc) {
        float target = (enabled && VisualsClient.zoomKey.isPressed()) ? (float) strength.value : 1f;
        current = smooth.asBool() ? current + (target - current) * 0.35f : target;
    }

    public float factor() { return current; }
}
