package com.example.visuals.modules;

public class FovModule extends Module {
    public final Setting bonus = add(Setting.number("FOV Bonus", 10, -30, 60));

    public FovModule() { super("FOV", "Adds to your field of view beyond the vanilla slider"); }
}
