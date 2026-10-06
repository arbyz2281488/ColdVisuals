package com.example.visuals.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public boolean enabled;
    public final List<Setting> settings = new ArrayList<>();

    /** GUI category: Interface, Helper, Visuals or Other. */
    public String category = "Other";
    /** Animation state for the GUI switch (0..1). */
    public float anim;

    // --- draggable HUD support ---
    public boolean movable;
    public double posX, posY;
    /** Last drawn rectangle on screen (used by the GUI for dragging). */
    public int boxX, boxY, boxW, boxH;

    // --- optional action key: a key bound in the GUI that triggers this module's action ---
    public boolean hasActionKey;
    public int actionKey = -1;
    public boolean actionWasDown;
    public String actionLabel = "Key";

    public boolean hasActionKey2;
    public int actionKey2 = -1;
    public boolean action2WasDown;
    public String action2Label = "Key 2";

    protected void enableActionKey() { hasActionKey = true; }

    protected void enableActionKey(String label) {
        hasActionKey = true;
        actionLabel = label;
    }

    protected void enableActionKey2(String label) {
        hasActionKey2 = true;
        action2Label = label;
    }

    /** Called when the first action key is pressed (only while the module is enabled). */
    public void onActionKey(MinecraftClient mc) {}

    /** Called when the second action key is pressed (only while the module is enabled). */
    public void onActionKey2(MinecraftClient mc) {}

    protected Module(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected Setting add(Setting s) {
        settings.add(s);
        return s;
    }

    protected void makeMovable(double defX, double defY) {
        movable = true;
        posX = defX;
        posY = defY;
    }

    protected void setBox(int x, int y, int w, int h) {
        boxX = x; boxY = y; boxW = w; boxH = h;
    }

    /** Keeps the element on screen and returns its X. */
    protected int clampX(DrawContext ctx, int w) {
        posX = Math.max(0, Math.min(posX, ctx.getScaledWindowWidth() - w));
        return (int) posX;
    }

    protected int clampY(DrawContext ctx, int h) {
        posY = Math.max(0, Math.min(posY, ctx.getScaledWindowHeight() - h));
        return (int) posY;
    }

    public void moveBy(double dx, double dy) {
        posX += dx;
        posY += dy;
    }

    public boolean hitBox(double mx, double my) {
        return movable && boxW > 0 && mx >= boxX && mx < boxX + boxW && my >= boxY && my < boxY + boxH;
    }

    public void toggle() { enabled = !enabled; }

    /** Called every tick, even when disabled (so effects can smoothly unwind). */
    public void onTick(MinecraftClient mc) {}

    /** Called every frame for HUD drawing, only when enabled. */
    public void onHud(DrawContext ctx, MinecraftClient mc) {}
}
