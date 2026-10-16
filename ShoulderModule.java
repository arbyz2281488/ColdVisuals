package com.example.visuals.modules;

/** Shrek, the Redstone Golem or a sword on your shoulder / back (see ShoulderRenderer). Only you see it. */
public class ShoulderModule extends Module {
    public final Setting model = add(Setting.number("Model 1Shrek 2Gol 3Sword", 1, 1, 3));
    public final Setting place = add(Setting.number("Place 1R 2L 3Back", 1, 1, 3));
    public final Setting scale = add(Setting.number("Scale %", 100, 30, 250));
    public final Setting height = add(Setting.number("Height", 0, -0.5, 0.5));
    public final Setting texture = add(Setting.number("Colour 0-6", 0, 0, 6));

    public ShoulderModule() { super("Shoulder", "Shrek / golem / sword on your shoulder (third person)"); }
}
