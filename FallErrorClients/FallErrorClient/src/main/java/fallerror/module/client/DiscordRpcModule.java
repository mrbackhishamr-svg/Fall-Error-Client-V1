package fallerror.module.client;

import fallerror.discord.DiscordRpc;
import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.BoolSetting;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.integrated.IntegratedServer;

public class DiscordRpcModule extends Module {
    final BoolSetting showName = add(new BoolSetting("Show Username", true));
    final BoolSetting showIp   = add(new BoolSetting("Show Server IP", true));
    private int timer;

    public DiscordRpcModule() {
        super("Discord RPC", "Shows name, server/world, version on your Discord profile", Category.SETTINGS);
        setEnabled(true);
    }

    @Override public void onEnable() { DiscordRpc.start(); }
    @Override public void onDisable() { DiscordRpc.stop(); }

    @Override
    public void onTick() {
        if (++timer < 40) return;
        timer = 0;
        String user = showName.get() ? mc.getSession().getUsername() : "Player";
        String where = "In the menus";
        if (mc.world != null) {
            ServerInfo si = mc.getCurrentServerEntry();
            if (si != null) {
                where = showIp.get() ? si.address : "Multiplayer";
            } else {
                IntegratedServer srv = mc.getServer();
                where = srv != null ? srv.getSaveProperties().getLevelName() : "Singleplayer";
            }
        }
        DiscordRpc.update(user + " | " + where, "Minecraft " + mc.getGameVersion() + " | Made by NoMoreLimits");
    }
}
