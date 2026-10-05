package com.example.visuals;

import com.example.visuals.modules.Module;
import com.example.visuals.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Handles chat commands that start with a dot: .config and .bind */
public class CommandHandler {

    /** Returns true if the message was a mod command (so it is NOT sent to the server). */
    public static boolean handle(String message) {
        String[] a = message.trim().split("\\s+");
        String cmd = a[0].toLowerCase(Locale.ROOT);
        if (cmd.equals(".config")) {
            config(a);
            return true;
        }
        if (cmd.equals(".bind")) {
            bind(a);
            return true;
        }
        if (cmd.equals(".key")) {
            key(a);
            return true;
        }
        if (cmd.equals(".gps")) {
            gps(a);
            return true;
        }
        if (cmd.equals(".friend")) {
            friend(a);
            return true;
        }
        if (cmd.equals(".cold")) {
            cold(a);
            return true;
        }
        if (cmd.equals(".pearl")) {
            ModuleManager.PEARL.use(MinecraftClient.getInstance(), Items.ENDER_PEARL);
            return true;
        }
        if (cmd.equals(".firework")) {
            ModuleManager.PEARL.use(MinecraftClient.getInstance(), Items.FIREWORK_ROCKET);
            return true;
        }
        if (cmd.equals(".elytraswap")) {
            ModuleManager.ELYTRASWAP.run(MinecraftClient.getInstance());
            return true;
        }
        return false;
    }

    private static void config(String[] a) {
        if (a.length < 2) {
            say(".config dir | list | load <имя> | add <имя> | clear <имя>");
            return;
        }
        String sub = a[1].toLowerCase(Locale.ROOT);
        try {
            switch (sub) {
                case "dir" -> {
                    Path dir = Config.configsDir();
                    openFolder(dir);
                    say("Папка конфигов: " + dir.toAbsolutePath());
                }
                case "list" -> {
                    List<String> names = Config.list();
                    say(names.isEmpty() ? "Конфигов пока нет. Создай: .config add <имя>"
                            : "Конфиги: " + String.join(", ", names));
                }
                case "add" -> {
                    String name = nameArg(a);
                    if (name == null) return;
                    Config.saveAs(name);
                    say("Конфиг '" + name + "' создан (сохранены модули, настройки, позиции и бинды).");
                }
                case "load" -> {
                    String name = nameArg(a);
                    if (name == null) return;
                    say(Config.loadFrom(name) ? "Конфиг '" + name + "' загружен." : "Конфиг '" + name + "' не найден.");
                }
                case "clear" -> {
                    String name = nameArg(a);
                    if (name == null) return;
                    say(Config.delete(name) ? "Конфиг '" + name + "' удалён." : "Конфиг '" + name + "' не найден.");
                }
                default -> say("Неизвестная подкоманда. .config dir | list | load | add | clear");
            }
        } catch (IOException e) {
            say("Ошибка файла: " + e.getMessage());
        }
    }

    private static String nameArg(String[] a) {
        if (a.length < 3) {
            say("Укажи название: .config " + a[1] + " <имя>");
            return null;
        }
        if (!Config.validName(a[2])) {
            say("Название: только буквы, цифры, _ и - (до 32 символов).");
            return null;
        }
        return a[2];
    }

    private static void bind(String[] a) {
        if (a.length < 2) {
            say(".bind add <модуль> <клавиша> | list | clear [модуль]");
            return;
        }
        String sub = a[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "add" -> {
                if (a.length < 4) {
                    say("Использование: .bind add <модуль> <клавиша>   (например .bind add Zoom R)");
                    return;
                }
                Module m = ModuleManager.byName(a[2]);
                if (m == null) {
                    say("Нет такого модуля. Доступно: " + moduleNames());
                    return;
                }
                int key = BindManager.parseKey(a[3]);
                if (key < 0) {
                    say("Неизвестная клавиша: " + a[3] + " (примеры: R, F, G, F5, RSHIFT, SPACE, NUMPAD1)");
                    return;
                }
                BindManager.set(m.name, key);
                Config.save();
                say(m.name + " привязан к клавише " + BindManager.keyName(key));
            }
            case "list" -> {
                Map<String, Integer> binds = BindManager.all();
                if (binds.isEmpty()) {
                    say("Биндов нет. Добавь: .bind add <модуль> <клавиша>");
                } else {
                    say("Бинды:");
                    for (var e : binds.entrySet()) say(" " + e.getKey() + " → " + BindManager.keyName(e.getValue()));
                }
            }
            case "clear" -> {
                if (a.length >= 3) {
                    Module m = ModuleManager.byName(a[2]);
                    if (m == null || !BindManager.all().containsKey(m.name)) {
                        say("У такого модуля нет бинда.");
                        return;
                    }
                    BindManager.remove(m.name);
                    say("Бинд " + m.name + " удалён.");
                } else {
                    BindManager.clear();
                    say("Все бинды удалены.");
                }
                Config.save();
            }
            default -> say("Неизвестная подкоманда. .bind add | list | clear");
        }
    }

    private static void key(String[] a) {
        if (a.length < 2) {
            say(".key add <название> <команда> <клавиша> | list | clear [название]");
            return;
        }
        String sub = a[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "add" -> {
                if (a.length < 5) {
                    say("Использование: .key add <название> <команда> <клавиша>   (например .key add spawn /spawn R)");
                    return;
                }
                String name = a[2];
                if (!Config.validName(name)) {
                    say("Название: только буквы, цифры, _ и - (до 32 символов).");
                    return;
                }
                int key = BindManager.parseKey(a[a.length - 1]);
                if (key < 0) {
                    say("Неизвестная клавиша: " + a[a.length - 1] + " (примеры: R, F, G, F5, RSHIFT, SPACE, NUMPAD1)");
                    return;
                }
                String command = String.join(" ", Arrays.copyOfRange(a, 3, a.length - 1));
                CommandBinds.set(name, command, key);
                Config.save();
                say("'" + name + "': " + command + " → клавиша " + BindManager.keyName(key));
            }
            case "list" -> {
                var binds = CommandBinds.all();
                if (binds.isEmpty()) {
                    say("Список пуст. Добавь: .key add <название> <команда> <клавиша>");
                } else {
                    say("Команды на клавишах:");
                    for (var e : binds.entrySet()) {
                        say(" " + e.getKey() + ": " + e.getValue().command()
                                + " → " + BindManager.keyName(e.getValue().key()));
                    }
                }
            }
            case "clear" -> {
                if (a.length >= 3) {
                    if (!CommandBinds.all().containsKey(a[2])) {
                        say("Нет бинда с названием '" + a[2] + "'.");
                        return;
                    }
                    CommandBinds.remove(a[2]);
                    say("Бинд '" + a[2] + "' удалён.");
                } else {
                    CommandBinds.clear();
                    say("Все команды на клавишах удалены.");
                }
                Config.save();
            }
            default -> say("Неизвестная подкоманда. .key add | list | clear");
        }
    }

    private static void gps(String[] a) {
        if (a.length < 2) {
            say(".gps set <X> <Z>   |   .gps off");
            return;
        }
        String sub = a[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "set" -> {
                if (a.length < 4) {
                    say("Использование: .gps set <X> <Z>   (например .gps set -120 340)");
                    return;
                }
                try {
                    double x = Double.parseDouble(a[2]);
                    double z = Double.parseDouble(a.length >= 5 ? a[4] : a[3]); // also accepts "X Y Z"
                    ModuleManager.GPS.setTarget(x, z);
                    say("GPS: метка на X " + Math.round(x) + " Z " + Math.round(z) + ". Отключить: .gps off");
                } catch (NumberFormatException e) {
                    say("Координаты должны быть числами, например: .gps set -120 340");
                }
            }
            case "off" -> {
                ModuleManager.GPS.off();
                say("GPS отключён.");
            }
            default -> say("Неизвестная подкоманда. .gps set <X> <Z> | .gps off");
        }
    }

    private static void friend(String[] a) {
        if (a.length < 2) {
            say(".friend add <ник> | remove <ник> | list");
            return;
        }
        String sub = a[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "add" -> {
                if (a.length < 3 || !a[2].matches("[A-Za-z0-9_]{1,16}")) {
                    say("Использование: .friend add <ник>");
                    return;
                }
                say(FriendManager.add(a[2]) ? "Друг добавлен: " + a[2] : a[2] + " уже в списке.");
            }
            case "remove" -> {
                if (a.length < 3) {
                    say("Использование: .friend remove <ник>");
                    return;
                }
                say(FriendManager.remove(a[2]) ? "Друг удалён: " + a[2] : a[2] + " нет в списке.");
            }
            case "list" -> {
                var friends = FriendManager.all();
                say(friends.isEmpty() ? "Список друзей пуст. Добавь: .friend add <ник>"
                        : "Друзья: " + String.join(", ", friends));
            }
            default -> say("Неизвестная подкоманда. .friend add | remove | list");
        }
    }

    private static void cold(String[] a) {
        if (a.length < 2) {
            say(".cold url <адрес> | list");
            return;
        }
        String sub = a[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "url" -> {
                if (a.length < 3) {
                    String u = ColdNet.url();
                    say(u.isEmpty() ? "Адрес сервера не задан. .cold url https://..." : "Адрес: " + u);
                    return;
                }
                ColdNet.setUrl(a[2]);
                say(ColdNet.url().isEmpty() ? "Адрес должен начинаться с https://" : "Адрес сохранён: " + ColdNet.url());
            }
            case "list" -> {
                var users = ColdNet.users();
                say(users.isEmpty() ? "Других пользователей Cold на этом сервере пока не найдено."
                        : "Пользователи Cold онлайн: " + String.join(", ", users));
            }
            default -> say("Неизвестная подкоманда. .cold url <адрес> | list");
        }
    }

    private static String moduleNames() {
        return ModuleManager.all().stream().map(m -> m.name).collect(Collectors.joining(", "));
    }

    private static void openFolder(Path dir) throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String path = dir.toAbsolutePath().toString();
        if (os.contains("win")) new ProcessBuilder("explorer.exe", path).start();
        else if (os.contains("mac")) new ProcessBuilder("open", path).start();
        else new ProcessBuilder("xdg-open", path).start();
    }

    private static void say(String text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(Text.literal("§d[ColdVisuals] §f" + text), false);
    }
}
