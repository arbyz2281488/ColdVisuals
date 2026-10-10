package com.example.visuals.modules;

/** Ice-blue main menu with your logo as the background (see MenuBackground and the menu mixins). */
public class MenuStyleModule extends Module {
    public final Setting snow = add(Setting.bool("Snow", true));
    public final Setting logo = add(Setting.number("Logo Opacity %", 55, 15, 100));
    public final Setting darken = add(Setting.number("Darken %", 30, 0, 80));
    /** Replace the window / taskbar icon with the ice "C" (takes effect on the next game start). */
    public final Setting windowIcon = add(Setting.bool("Window Icon", true));

    public MenuStyleModule() {
        super("MenuStyle", "Ice background and branding on the main menu");
        enabled = true; // on by default (a saved config can still turn it off)
    }
}
