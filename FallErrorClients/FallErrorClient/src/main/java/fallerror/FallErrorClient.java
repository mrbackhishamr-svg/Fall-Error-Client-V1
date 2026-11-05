package fallerror;

import fallerror.gui.ClickGui;
import fallerror.module.ModuleManager;
import fallerror.util.Friends;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class FallErrorClient implements ClientModInitializer {
    public static final String NAME = "Fall Error Client";
    public static ModuleManager modules;
    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fallerror.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of("fallerror", "main"))));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openGui.wasPressed()) mc.setScreen(new ClickGui());
            modules.tick();
        });
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            modules.render2d(ctx);
            modules.hud.render(ctx);
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> Friends.registerCommands(dispatcher));
    }
}
