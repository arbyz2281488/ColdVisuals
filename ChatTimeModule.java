package com.example.visuals.modules;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Adds a [HH:mm] stamp to incoming chat messages (see Hooks). */
public class ChatTimeModule extends Module {
    public final Setting seconds = add(Setting.bool("Show Seconds", false));

    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter LONG = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ChatTimeModule() { super("ChatTime", "Adds the time before chat messages"); }

    public String stamp() {
        return LocalTime.now().format(seconds.asBool() ? LONG : SHORT);
    }
}
