package com.example.visuals.modules;

/** When a new message arrives the chat slides up into place instead of jumping (see ChatHudMixin). */
public class SmoothChatModule extends Module {
    public final Setting duration = add(Setting.number("Duration ms", 220, 100, 500));
    public final Setting distance = add(Setting.number("Distance", 9, 3, 20));

    /** True while the mixin has pushed a matrix that still has to be popped. */
    public boolean pushed;
    private long last;

    public SmoothChatModule() { super("SmoothChat", "Chat slides up when a message arrives"); }

    public void onMessage() { last = System.currentTimeMillis(); }

    /** Current vertical offset in pixels (0 when the animation is over). */
    public float offset() {
        float t = (System.currentTimeMillis() - last) / (float) duration.value;
        if (t >= 1f) return 0f;
        float ease = 1f - (1f - t) * (1f - t) * (1f - t);
        return (1f - ease) * (float) distance.value;
    }
}
