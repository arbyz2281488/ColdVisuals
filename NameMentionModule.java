package com.example.visuals.modules;

/** Alerts you when your nickname appears in chat (see Hooks). */
public class NameMentionModule extends Module {
    public final Setting sound = add(Setting.bool("Sound", true));
    public final Setting highlight = add(Setting.bool("Highlight", true));

    public NameMentionModule() { super("NameMention", "Sound and highlight when your nick is mentioned in chat"); }
}
