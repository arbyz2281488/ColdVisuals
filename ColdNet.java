package com.example.visuals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Lets clients with these visuals find each other.
 * Every ~45 s the client tells YOUR small backend "this nickname is online on this server" and asks who else is.
 * Only active when the ColdTag module is on, the backend URL is set, and you are on a multiplayer server.
 * Data sent: your nickname and the server address. Backend code: see cold-backend/worker.js
 */
public class ColdNet {
    /** Paste your backend address here before building so everybody who gets the jar uses it. */
    public static final String DEFAULT_URL = "";

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visuals-cloud.txt");
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private static String url;
    private static volatile Set<String> users = Set.of();
    private static String lastServer;
    private static int countdown;

    public static String url() {
        if (url == null) {
            String u = DEFAULT_URL;
            try {
                if (Files.exists(FILE)) {
                    String s = Files.readString(FILE).trim();
                    if (!s.isEmpty()) u = s;
                }
            } catch (IOException ignored) {}
            url = clean(u);
        }
        return url;
    }

    public static void setUrl(String u) {
        url = clean(u);
        try {
            Files.writeString(FILE, url);
        } catch (IOException ignored) {}
    }

    private static String clean(String u) {
        u = u == null ? "" : u.trim();
        while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
        return (u.startsWith("https://") || u.startsWith("http://")) ? u : "";
    }

    public static Set<String> users() { return users; }

    public static boolean isUser(String name) {
        return users.contains(name.toLowerCase(Locale.ROOT));
    }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null) {
            users = Set.of();
            lastServer = null;
            countdown = 0;
            return;
        }
        String u = url();
        if (u.isEmpty()) return;
        ServerInfo info = mc.getCurrentServerEntry();
        if (info == null) return; // singleplayer: nothing to do
        String server = info.address.toLowerCase(Locale.ROOT);
        if (!server.equals(lastServer)) {
            users = Set.of();
            lastServer = server;
            countdown = 0;
        }
        if (countdown-- > 0) return;
        countdown = 20 * 45;
        ping(u, server, mc.player.getName().getString());
        fetch(u, server);
    }

    private static void ping(String u, String server, String name) {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("server", server);
            o.addProperty("name", name);
            HttpRequest req = HttpRequest.newBuilder(URI.create(u + "/ping"))
                    .timeout(Duration.ofSeconds(6))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(o.toString()))
                    .build();
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding()).exceptionally(t -> null);
        } catch (IllegalArgumentException ignored) {}
    }

    private static void fetch(String u, String server) {
        try {
            HttpRequest req = HttpRequest.newBuilder(
                            URI.create(u + "/list?server=" + URLEncoder.encode(server, StandardCharsets.UTF_8)))
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> {
                        if (res.statusCode() != 200) return;
                        try {
                            JsonArray arr = JsonParser.parseString(res.body()).getAsJsonArray();
                            Set<String> fresh = new HashSet<>();
                            for (JsonElement e : arr) fresh.add(e.getAsString().toLowerCase(Locale.ROOT));
                            users = fresh;
                        } catch (Exception ignored) {}
                    })
                    .exceptionally(t -> null);
        } catch (IllegalArgumentException ignored) {}
    }
}
