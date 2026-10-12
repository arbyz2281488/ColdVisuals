package com.example.visuals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Minimal client for the Modrinth API (https://docs.modrinth.com): search and install mods, resource packs and
 * shader packs. Files are only taken from cdn.modrinth.com and their SHA-512 hash is checked before they are kept.
 */
public class Modrinth {
    public static final String MOD = "mod", PACK = "resourcepack", SHADER = "shader";

    public record Hit(String id, String slug, String title, String author, String description, long downloads) {}

    private static final String API = "https://api.modrinth.com/v2";
    private static final String USER_AGENT = "ColdVisuals/1.0 (in-game Minecraft mod browser)";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static String gameVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft")
                .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("1.21.4");
    }

    public static String folderName(String type) {
        return switch (type) {
            case PACK -> "resourcepacks";
            case SHADER -> "shaderpacks";
            default -> "mods";
        };
    }

    public static Path folder(String type) {
        Path dir = FabricLoader.getInstance().getGameDir().resolve(folderName(type));
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {}
        return dir;
    }

    public static void openFolder(String type) throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String path = folder(type).toAbsolutePath().toString();
        if (os.contains("win")) new ProcessBuilder("explorer.exe", path).start();
        else if (os.contains("mac")) new ProcessBuilder("open", path).start();
        else new ProcessBuilder("xdg-open", path).start();
    }

    public static boolean isInstalled(String slug) {
        if (slug == null || slug.isEmpty()) return false;
        FabricLoader l = FabricLoader.getInstance();
        return l.isModLoaded(slug) || l.isModLoaded(slug.replace('-', '_'));
    }

    // ------------------------------------------------------------------ http / json helpers

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String get(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IOException("Modrinth ответил: HTTP " + res.statusCode());
        return res.body();
    }

    private static String str(JsonObject o, String key) {
        return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    // ------------------------------------------------------------------ search

    public static List<Hit> search(String query, String type, boolean byDownloads) throws IOException, InterruptedException {
        String facets = "[[\"project_type:" + type + "\"],[\"versions:" + gameVersion() + "\"]"
                + (type.equals(MOD) ? ",[\"categories:fabric\"]" : "") + "]";
        String url = API + "/search?query=" + enc(query) + "&facets=" + enc(facets)
                + "&limit=25&index=" + (byDownloads ? "downloads" : "relevance");
        JsonObject root = JsonParser.parseString(get(url)).getAsJsonObject();
        List<Hit> out = new ArrayList<>();
        for (JsonElement e : root.getAsJsonArray("hits")) {
            JsonObject o = e.getAsJsonObject();
            out.add(new Hit(str(o, "project_id"), str(o, "slug"), str(o, "title"), str(o, "author"),
                    str(o, "description"), o.has("downloads") && !o.get("downloads").isJsonNull() ? o.get("downloads").getAsLong() : 0));
        }
        return out;
    }

    // ------------------------------------------------------------------ install

    /** Installs a project (and the mods it requires). Returns a short summary for the user. */
    public static String install(Hit hit, String type, Consumer<String> status) throws IOException, InterruptedException {
        List<String> installed = new ArrayList<>();
        installProject(hit.id(), hit.slug(), type, status, new HashSet<>(), 0, installed);
        return installed.isEmpty() ? "Уже установлено: " + hit.title() : "Скачано: " + String.join(", ", installed);
    }

    private static void installProject(String id, String slug, String type, Consumer<String> status,
                                       Set<String> visited, int depth, List<String> installed)
            throws IOException, InterruptedException {
        if (depth > 3 || !visited.add(id)) return;
        if (type.equals(MOD) && isInstalled(slug)) return;

        status.accept("Ищу версию: " + (slug.isEmpty() ? id : slug) + "...");
        JsonObject version = latestVersion(id, type);
        if (version == null) {
            throw new IOException("нет версии под " + gameVersion() + (type.equals(MOD) ? " (Fabric)" : ""));
        }

        if (type.equals(MOD) && version.has("dependencies") && version.get("dependencies").isJsonArray()) {
            for (JsonElement d : version.getAsJsonArray("dependencies")) {
                JsonObject dep = d.getAsJsonObject();
                if (!"required".equals(str(dep, "dependency_type"))) continue;
                String pid = str(dep, "project_id");
                if (pid.isEmpty()) continue;
                installProject(pid, projectSlug(pid), MOD, status, visited, depth + 1, installed);
            }
        }
        download(version, type, status, installed);
    }

    private static String projectSlug(String id) {
        try {
            return str(JsonParser.parseString(get(API + "/project/" + enc(id))).getAsJsonObject(), "slug");
        } catch (Exception e) {
            return "";
        }
    }

    private static JsonObject latestVersion(String id, String type) throws IOException, InterruptedException {
        String url = API + "/project/" + enc(id) + "/version?game_versions=" + enc("[\"" + gameVersion() + "\"]")
                + (type.equals(MOD) ? "&loaders=" + enc("[\"fabric\"]") : "");
        JsonArray arr = JsonParser.parseString(get(url)).getAsJsonArray();
        if (arr.isEmpty()) return null;
        for (JsonElement e : arr) {
            JsonObject o = e.getAsJsonObject();
            if ("release".equals(str(o, "version_type"))) return o;
        }
        return arr.get(0).getAsJsonObject();
    }

    private static void download(JsonObject version, String type, Consumer<String> status, List<String> installed)
            throws IOException, InterruptedException {
        JsonArray files = version.getAsJsonArray("files");
        if (files == null || files.isEmpty()) throw new IOException("в версии нет файлов");
        JsonObject file = null;
        for (JsonElement e : files) {
            JsonObject f = e.getAsJsonObject();
            if (f.has("primary") && f.get("primary").getAsBoolean()) {
                file = f;
                break;
            }
        }
        if (file == null) file = files.get(0).getAsJsonObject();

        URI uri = URI.create(str(file, "url"));
        if (!"cdn.modrinth.com".equals(uri.getHost())) throw new IOException("неожиданный адрес загрузки");
        String name = Path.of(str(file, "filename")).getFileName().toString();
        String ext = type.equals(MOD) ? ".jar" : ".zip";
        if (!name.toLowerCase(Locale.ROOT).endsWith(ext)) throw new IOException("неожиданный тип файла: " + name);

        Path dir = folder(type);
        Path target = dir.resolve(name);
        if (Files.exists(target)) return;
        Path tmp = dir.resolve(name + ".part");

        status.accept("Скачиваю " + name + "...");
        HttpRequest req = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMinutes(5))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();
        HttpResponse<InputStream> res = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (res.statusCode() != 200) throw new IOException("загрузка не удалась: HTTP " + res.statusCode());

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("нет SHA-512", e);
        }
        try (InputStream in = res.body(); OutputStream out = Files.newOutputStream(tmp)) {
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) {
                md.update(buf, 0, n);
                out.write(buf, 0, n);
            }
        }

        String got = HexFormat.of().formatHex(md.digest());
        String want = file.has("hashes") && file.get("hashes").isJsonObject() ? str(file.getAsJsonObject("hashes"), "sha512") : "";
        if (!want.isEmpty() && !want.equalsIgnoreCase(got)) {
            Files.deleteIfExists(tmp);
            throw new IOException("контрольная сумма не совпала, файл удалён");
        }
        Files.move(tmp, target);
        installed.add(name);
    }
}
