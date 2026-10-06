package com.example.visuals.modules;

public class Setting {
    public enum Type { BOOL, NUMBER }

    public final String name;
    public final Type type;
    public double value;
    public final double min, max;
    /** Animation state for the GUI switch (0..1). */
    public float anim;

    private Setting(String name, Type type, double def, double min, double max) {
        this.name = name; this.type = type; this.value = def; this.min = min; this.max = max;
    }

    public static Setting bool(String name, boolean def) {
        return new Setting(name, Type.BOOL, def ? 1 : 0, 0, 1);
    }

    public static Setting number(String name, double def, double min, double max) {
        return new Setting(name, Type.NUMBER, def, min, max);
    }

    public boolean asBool() { return value != 0; }
    public int asInt() { return (int) Math.round(value); }
}
