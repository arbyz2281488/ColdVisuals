package com.example.visuals.modules;

/** Stretches the picture horizontally (below 1 = wider picture, above 1 = narrower). */
public class ScreenStretchModule extends Module {
    public final Setting stretch = add(Setting.number("Stretch", 0.8, 0.5, 1.5));

    public ScreenStretchModule() { super("ScreenStretch", "Stretch the picture horizontally"); }
}
