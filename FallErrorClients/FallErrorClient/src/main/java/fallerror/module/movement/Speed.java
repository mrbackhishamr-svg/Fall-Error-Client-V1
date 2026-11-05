package fallerror.module.movement;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.*;
import fallerror.util.Util;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
    final NumberSetting speed = add(new NumberSetting("Speed", 0.35, 0.1, 1.0, ""));
    final BoolSetting groundOnly = add(new BoolSetting("Ground Only", true));

    public Speed() { super("Speed", "Move faster", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        if (groundOnly.get() && !mc.player.isOnGround()) return;
        double[] d = Util.moveDir();
        if (d[0] == 0 && d[1] == 0) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(d[0] * speed.get(), v.y, d[1] * speed.get());
    }
}
