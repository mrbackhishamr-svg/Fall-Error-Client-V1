package fallerror.module.combat;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

public class Reach extends Module {
    final NumberSetting entityReach = add(new NumberSetting("Entity Reach", 3.5, 3, 6, " blocks"));
    final NumberSetting blockReach  = add(new NumberSetting("Block Reach", 4.5, 4.5, 8, " blocks"));

    public Reach() {
        super("Reach", "Extends how far you can hit and interact", Category.COMBAT);
    }

    private void set(double entity, double block) {
        if (mc.player == null) return;
        EntityAttributeInstance e = mc.player.getAttributeInstance(EntityAttributes.ENTITY_INTERACTION_RANGE);
        EntityAttributeInstance b = mc.player.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
        if (e != null) e.setBaseValue(entity);
        if (b != null) b.setBaseValue(block);
    }

    @Override public void onTick() { set(entityReach.get(), blockReach.get()); }
    @Override public void onDisable() { set(3.0, 4.5); }
}
