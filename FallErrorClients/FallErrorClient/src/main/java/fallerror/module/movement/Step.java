package fallerror.module.movement;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.NumberSetting;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

public class Step extends Module {
    final NumberSetting height = add(new NumberSetting("Height", 1.0, 0.6, 2.5, " blocks"));

    public Step() { super("Step", "Walk up blocks without jumping", Category.MOVEMENT); }

    private void set(double h) {
        if (mc.player == null) return;
        EntityAttributeInstance a = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
        if (a != null) a.setBaseValue(h);
    }
    @Override public void onTick() { set(height.get()); }
    @Override public void onDisable() { set(0.6); }
}
