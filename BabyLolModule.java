package com.example.visuals.modules;

/** Draws players smaller on your screen (see LivingEntityRendererMixin). Affects everyone, you included. */
public class BabyLolModule extends Module {
    public final Setting scale = add(Setting.number("Scale", 0.6, 0.3, 1.0));

    /** Number of matrix pushes waiting for their pop (used by the mixin). */
    public int pushed;

    public BabyLolModule() { super("BabyLol", "Players look small (visual only)"); }
}
