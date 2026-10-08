package com.example.visuals.modules;

/** Tints the sky (see ClientWorldMixin). */
public class SkyColorModule extends Module {
    public final Setting r = add(Setting.number("Red", 120, 0, 255));
    public final Setting g = add(Setting.number("Green", 90, 0, 255));
    public final Setting b = add(Setting.number("Blue", 255, 0, 255));
    public final Setting blend = add(Setting.number("Strength %", 100, 0, 100));

    public SkyColorModule() { super("SkyColor", "Custom sky colour"); }
}
