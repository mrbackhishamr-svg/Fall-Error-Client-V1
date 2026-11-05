package fallerror.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/** Friend list saved in config/fallerror_friends.txt. Friends are skipped by combat modules. */
public final class Friends {
    private static final Set<String> names = new LinkedHashSet<>();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fallerror_friends.txt");
    public static boolean enabled = true;
    static { load(); }
    private Friends() {}

    public static boolean isFriend(Entity e) {
        return enabled && e instanceof PlayerEntity p && names.contains(p.getName().getString().toLowerCase(Locale.ROOT));
    }
    public static boolean has(String n) { return names.contains(n.toLowerCase(Locale.ROOT)); }
    public static boolean add(String n) { boolean r = names.add(n.toLowerCase(Locale.ROOT)); if (r) save(); return r; }
    public static boolean remove(String n) { boolean r = names.remove(n.toLowerCase(Locale.ROOT)); if (r) save(); return r; }
    public static boolean toggle(String n) { if (has(n)) { remove(n); return false; } add(n); return true; }

    private static void load() {
        try {
            if (Files.exists(FILE)) for (String l : Files.readAllLines(FILE)) if (!l.isBlank()) names.add(l.trim().toLowerCase(Locale.ROOT));
        } catch (IOException ignored) {}
    }
    private static void save() {
        try { Files.write(FILE, names); } catch (IOException ignored) {}
    }

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> d) {
        d.register(ClientCommandManager.literal("friend")
            .then(ClientCommandManager.literal("add")
                .then(ClientCommandManager.argument("name", StringArgumentType.word()).executes(c -> {
                    String n = StringArgumentType.getString(c, "name");
                    c.getSource().sendFeedback(Text.literal(add(n) ? "Added friend: " + n : n + " is already a friend"));
                    return 1;
                })))
            .then(ClientCommandManager.literal("remove")
                .then(ClientCommandManager.argument("name", StringArgumentType.word()).executes(c -> {
                    String n = StringArgumentType.getString(c, "name");
                    c.getSource().sendFeedback(Text.literal(remove(n) ? "Removed friend: " + n : n + " is not a friend"));
                    return 1;
                })))
            .then(ClientCommandManager.literal("list").executes(c -> {
                c.getSource().sendFeedback(Text.literal(names.isEmpty() ? "No friends yet" : "Friends: " + String.join(", ", names)));
                return 1;
            }))
            .then(ClientCommandManager.literal("clear").executes(c -> {
                names.clear(); save();
                c.getSource().sendFeedback(Text.literal("Friend list cleared"));
                return 1;
            })));
    }
}
