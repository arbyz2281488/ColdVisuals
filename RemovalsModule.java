package com.example.visuals.modules;

/** Removes annoying screen effects (see the mixins). */
public class RemovalsModule extends Module {
    public final Setting hurtCam = add(Setting.bool("No Hurt Shake", true));
    public final Setting fire = add(Setting.bool("No Fire Overlay", true));

    public RemovalsModule() { super("Removals", "Removes camera shake on damage and the fire overlay"); }
}
