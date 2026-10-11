package com.example.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/** Writes errors from risky parts of the mod to .minecraft/config/visuals-error.log instead of crashing. */
public class ErrorLog {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visuals-error.log");

    public static void write(String where, Throwable t) {
        try {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            Files.writeString(FILE, "[" + Instant.now() + "] " + where + "\n" + sw + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Throwable ignored) {}
    }
}
