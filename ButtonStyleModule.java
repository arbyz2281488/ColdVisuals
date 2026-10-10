package com.example.visuals.modules;

import net.minecraft.client.gui.widget.PressableWidget;

import java.util.Map;
import java.util.WeakHashMap;

/** Ice-blue rounded buttons in every menu (see PressableWidgetMixin). */
public class ButtonStyleModule extends Module {
    public final Setting glow = add(Setting.bool("Hover Glow", true));

    private final Map<PressableWidget, Float> hover = new WeakHashMap<>();

    public ButtonStyleModule() {
        super("ButtonStyle", "Rounded ice-blue buttons in menus");
        enabled = true; // on by default (a saved config can still turn it off)
    }

    /** Smooth 0..1 hover animation for one button. */
    public float hover(PressableWidget w, boolean hovered) {
        float h = hover.getOrDefault(w, 0f);
        h += ((hovered ? 1f : 0f) - h) * 0.3f;
        hover.put(w, h);
        return h;
    }
}
