package fallerror.module.movement;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.NumberSetting;
import fallerror.util.Util;

public class Fly extends Module {
    final NumberSetting speed = add(new NumberSetting("Speed", 1.0, 0.1, 5.0, ""));

    public Fly() { super("Fly", "Fly freely: Space goes up, Shift goes down", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        double s = speed.get() * 0.5;
        double[] d = Util.moveDir();
        double vy = mc.options.jumpKey.isPressed() ? s : mc.options.sneakKey.isPressed() ? -s : 0;
        mc.player.setVelocity(d[0] * s, vy, d[1] * s);
    }
}
