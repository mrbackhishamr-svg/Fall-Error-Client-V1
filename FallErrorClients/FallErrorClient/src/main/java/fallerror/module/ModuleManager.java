package fallerror.module;

import fallerror.module.client.DiscordRpcModule;
import fallerror.module.client.Hud;
import fallerror.module.client.Interface;
import fallerror.module.combat.*;
import fallerror.module.items.AutoTotem;
import fallerror.module.movement.*;
import fallerror.module.visuals.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public final List<Module> modules = new ArrayList<>();
    public final Hud hud = new Hud();

    public ModuleManager() {
        // Combat
        modules.add(new AimAssist());
        modules.add(new TriggerBot());
        modules.add(new KillAura());
        modules.add(new AutoMace());
        modules.add(new AutoSpear());
        modules.add(new AutoClicker());
        modules.add(new Reach());
        modules.add(new FriendsModule());
        // Movement
        modules.add(new Sprint());
        modules.add(new Fly());
        modules.add(new Speed());
        modules.add(new Step());
        // Items
        modules.add(new AutoTotem());
        // Visuals
        modules.add(new ESPPlayers());
        modules.add(new ESPItem());
        modules.add(new ESPChest());
        modules.add(new Xray());
        modules.add(new Fullbright());
        // Settings
        modules.add(new Interface());
        modules.add(hud);
        modules.add(new DiscordRpcModule());
    }

    public List<Module> byCategory(Category c) {
        return modules.stream().filter(m -> m.category == c).toList();
    }

    public void tick() {
        for (Module m : modules) if (m.isEnabled()) m.onTick();
    }

    public void render2d(DrawContext g) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
        for (Module m : modules) if (m.isEnabled()) m.onRender2D(g);
    }
}
