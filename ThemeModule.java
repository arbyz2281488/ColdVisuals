package com.example.visuals.modules;

/** Accent colour of the Cold GUI and buttons: 1 = ice blue, 2 = purple, 3 = mint, 4 = pink. */
public class ThemeModule extends Module {
    public final Setting accent = add(Setting.number("Accent 1Ice 2Pur 3Mint 4Pink", 1, 1, 4));

    public ThemeModule() {
        super("Theme", "Accent colour of the menus");
        enabled = true;
    }

    public int accent() {
        return switch (accent.asInt()) {
            case 2 -> 0xFFB06CFF;
            case 3 -> 0xFF66E08A;
            case 4 -> 0xFFFF7AC6;
            default -> 0xFF6CC8FF;
        };
    }
}
