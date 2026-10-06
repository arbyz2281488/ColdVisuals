package com.example.visuals.modules;

/** Blocks your attacks on players from your friend list (see Hooks). Manage friends with .friend */
public class FriendSaveModule extends Module {
    public FriendSaveModule() {
        super("FriendSave", "You can't hit players from your friend list");
        enabled = true; // protection is on by default (a saved config can still turn it off)
    }
}
