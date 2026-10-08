package com.example.visuals.modules;

/** Changes how far you can see before fog starts and the colour of the fog (see BackgroundRendererMixin). */
public class CustomFogModule extends Module {
    public final Setting distance = add(Setting.number("Distance x", 1.5, 0.3, 3.0));
    public final Setting noFog = add(Setting.bool("No Fog", false));
    public final Setting recolor = add(Setting.bool("Custom Colour", false));
    public final Setting r = add(Setting.number("Red", 170, 0, 255));
    public final Setting g = add(Setting.number("Green", 120, 0, 255));
    public final Setting b = add(Setting.number("Blue", 255, 0, 255));

    public CustomFogModule() { super("CustomFog", "Fog distance and colour (Blindness/Darkness keep their fog)"); }
}
