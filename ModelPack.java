package com.example.visuals;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Builds the resource pack folder  .minecraft/resourcepacks/ColdModels  from the weapon models built into the mod.
 * You choose which model and which colour variant goes on each sword / the bow; the pack is rewritten and the
 * game reloads its resources. (Enable the pack "ColdModels" once in Options > Resource Packs.)
 */
public final class ModelPack {
    public static final String[] ITEMS = {"wooden_sword", "stone_sword", "golden_sword", "iron_sword",
            "diamond_sword", "netherite_sword", "bow"};
    public static final String[] ITEM_NAMES = {"Деревянный меч", "Каменный меч", "Золотой меч", "Железный меч",
            "Алмазный меч", "Незеритовый меч", "Лук"};
    public static final String[] VARIANTS = {"Оригинал", "Лёд", "Огонь", "Яд", "Фиолет", "Золото", "Тьма"};

    /** Index into ModelPackData.SLUGS per item, -1 = vanilla look. */
    public static final int[] model = {0, -1, -1, 1, -1, 2, 3};
    public static final int[] variant = new int[ITEMS.length];

    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("visuals-models.properties");
    private static boolean loaded, ensured;

    private ModelPack() {}

    public static Path packDir() {
        return FabricLoader.getInstance().getGameDir().resolve("resourcepacks").resolve("ColdModels");
    }

    public static void load() {
        if (loaded) return;
        loaded = true;
        try {
            if (!Files.exists(CONFIG)) return;
            Properties p = new Properties();
            try (var in = Files.newInputStream(CONFIG)) {
                p.load(in);
            }
            for (int i = 0; i < ITEMS.length; i++) {
                String v = p.getProperty(ITEMS[i]);
                if (v == null) continue;
                String[] parts = v.split(":");
                model[i] = Math.max(-1, Math.min(ModelPackData.SLUGS.length - 1, Integer.parseInt(parts[0])));
                variant[i] = parts.length > 1 ? Math.max(0, Math.min(Recolor.COUNT - 1, Integer.parseInt(parts[1]))) : 0;
            }
        } catch (Exception ignored) {}
    }

    public static void save() {
        try {
            Properties p = new Properties();
            for (int i = 0; i < ITEMS.length; i++) p.setProperty(ITEMS[i], model[i] + ":" + variant[i]);
            try (var out = Files.newOutputStream(CONFIG)) {
                p.store(out, "Cold weapon models");
            }
        } catch (IOException ignored) {}
    }

    /** Creates the pack with the default choice the first time the mod runs. */
    public static void ensureDefaults() {
        if (ensured) return;
        ensured = true;
        try {
            load();
            if (!Files.exists(packDir().resolve("pack.mcmeta"))) apply();
        } catch (Throwable t) {
            ErrorLog.write("ModelPack.ensureDefaults", t);
        }
    }

    private static byte[] decode(String[] chunks) {
        return Base64.getDecoder().decode(String.join("", chunks));
    }

    private static void deleteTree(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (Stream<Path> s = Files.walk(p)) {
            for (Path x : s.sorted(Comparator.reverseOrder()).toList()) Files.delete(x);
        }
    }

    /** Rewrites the whole pack from the current choice. */
    public static void apply() throws IOException {
        save();
        Path dir = packDir();
        deleteTree(dir.resolve("assets"));
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("pack.mcmeta"),
                "{\"pack\":{\"pack_format\":46,\"description\":\"Cold models\"}}", StandardCharsets.UTF_8);

        Set<String> written = new HashSet<>();
        for (int i = 0; i < ITEMS.length; i++) {
            int m = model[i];
            if (m < 0 || m >= ModelPackData.SLUGS.length) continue;
            int v = Math.max(0, Math.min(variant[i], Recolor.COUNT - 1));
            String name = ModelPackData.SLUGS[m] + "_v" + v;
            if (written.add(name)) writeModel(dir, m, v, name);

            Path items = dir.resolve("assets/minecraft/items");
            Files.createDirectories(items);
            Files.writeString(items.resolve(ITEMS[i] + ".json"),
                    "{\"model\":{\"type\":\"minecraft:model\",\"model\":\"coldmodels:item/" + name + "\"}}",
                    StandardCharsets.UTF_8);
        }
    }

    private static void writeModel(Path dir, int m, int v, String name) throws IOException {
        String slug = ModelPackData.SLUGS[m];
        String json = new String(decode(ModelPackData.MODEL[m]), StandardCharsets.UTF_8)
                .replace("item/" + slug + "_", "item/" + name + "_");
        Path models = dir.resolve("assets/coldmodels/models/item");
        Path textures = dir.resolve("assets/coldmodels/textures/item");
        Files.createDirectories(models);
        Files.createDirectories(textures);
        Files.writeString(models.resolve(name + ".json"), json, StandardCharsets.UTF_8);

        for (int j = 0; j < ModelPackData.TEX[m].length; j++) {
            byte[] png = Recolor.apply(decode(ModelPackData.TEX[m][j]), v);
            Files.write(textures.resolve(name + "_" + j + ".png"), png);
            int ft = ModelPackData.FRAMETIME[m][j];
            if (ft > 0) {
                Files.writeString(textures.resolve(name + "_" + j + ".png.mcmeta"),
                        "{\"animation\":{\"frametime\":" + ft + "}}", StandardCharsets.UTF_8);
            }
        }
    }
}
