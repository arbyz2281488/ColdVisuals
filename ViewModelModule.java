package com.example.visuals.modules;

/** Moves and resizes your hands / held items in first person (see HeldItemRendererMixin). */
public class ViewModelModule extends Module {
    public final Setting x = add(Setting.number("Offset X", 0, -1.0, 1.0));
    public final Setting y = add(Setting.number("Offset Y", 0, -1.0, 1.0));
    public final Setting z = add(Setting.number("Offset Z", 0, -1.0, 1.0));
    public final Setting scale = add(Setting.number("Scale", 1.0, 0.3, 1.5));

    /** Number of matrix pushes waiting for their pop (used by the mixin). */
    public int pushed;

    public ViewModelModule() { super("ViewModel", "Move and resize your hands"); }
}
