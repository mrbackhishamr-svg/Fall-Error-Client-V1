package fallerror.module.movement;

import fallerror.module.Category;
import fallerror.module.Module;

public class Sprint extends Module {
    public Sprint() { super("Sprint", "Always sprint while moving forward", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.options.forwardKey.isPressed() && !mc.player.isSneaking()
                && mc.player.getHungerManager().getFoodLevel() > 6) mc.player.setSprinting(true);
    }
}
