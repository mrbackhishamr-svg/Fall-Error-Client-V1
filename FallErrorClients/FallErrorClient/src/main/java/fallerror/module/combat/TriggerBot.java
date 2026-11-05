package fallerror.module.combat;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.*;
import fallerror.util.Util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

public class TriggerBot extends Module {
    final NumberSetting cooldown = add(new NumberSetting("Min Cooldown", 92, 0, 100, "%"));
    final BoolSetting   players  = add(new BoolSetting("Players", true));
    final BoolSetting   mobs     = add(new BoolSetting("Mobs", false));
    final BoolSetting   animals  = add(new BoolSetting("Animals", false));
    final BoolSetting   skipTeam = add(new BoolSetting("Skip Teammates", true));

    public TriggerBot() {
        super("Trigger Bot", "Attacks whatever is under your crosshair", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        if (mc.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity le && le.isAlive()
                && Util.valid(le, players.get(), mobs.get(), animals.get(), skipTeam.get())
                && mc.player.getAttackCooldownProgress(0f) >= cooldown.get() / 100.0) {
            Util.attack(le);
        }
    }
}
