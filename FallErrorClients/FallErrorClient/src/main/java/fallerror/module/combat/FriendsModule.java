package fallerror.module.combat;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.BoolSetting;
import fallerror.util.Friends;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

/** Friends are skipped by Aim Assist, Kill Aura, Trigger Bot, Auto Clicker, Auto Mace, Auto Spear. */
public class FriendsModule extends Module {
    final BoolSetting middleClick = add(new BoolSetting("Middle Click Add", true));
    private boolean wasDown;

    public FriendsModule() {
        super("Friends", "Protects friends. /friend add <name> or middle-click a player", Category.COMBAT);
        setEnabled(true);
    }

    @Override public void onEnable() { Friends.enabled = true; }
    @Override public void onDisable() { Friends.enabled = false; }

    @Override
    public void onTick() {
        if (mc.player == null || !middleClick.get()) return;
        boolean down = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;
        if (down && !wasDown && mc.currentScreen == null
                && mc.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof PlayerEntity p) {
            String n = p.getName().getString();
            boolean added = Friends.toggle(n);
            mc.player.sendMessage(Text.literal((added ? "Added friend: " : "Removed friend: ") + n), true);
        }
        wasDown = down;
    }
}
