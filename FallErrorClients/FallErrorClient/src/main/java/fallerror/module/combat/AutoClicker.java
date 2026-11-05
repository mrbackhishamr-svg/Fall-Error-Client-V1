package fallerror.module.combat;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.*;
import fallerror.util.Friends;
import fallerror.util.Util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

import java.util.concurrent.ThreadLocalRandom;

public class AutoClicker extends Module {
    final NumberSetting minCps = add(new NumberSetting("Min CPS", 8, 1, 20, ""));
    final NumberSetting maxCps = add(new NumberSetting("Max CPS", 12, 1, 20, ""));
    final BoolSetting   respectCooldown = add(new BoolSetting("Respect Cooldown", false));
    private long next;

    public AutoClicker() {
        super("Auto Clicker", "Clicks for you while you hold attack", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null || !mc.options.attackKey.isPressed()) return;
        long now = System.currentTimeMillis();
        if (now < next) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity le)) return;
        if (respectCooldown.get() && mc.player.getAttackCooldownProgress(0f) < 0.9f) return;
        if (Friends.isFriend(le)) return;
        Util.attack(le);
        double lo = Math.min(minCps.get(), maxCps.get()), hi = Math.max(minCps.get(), maxCps.get());
        double cps = lo == hi ? lo : ThreadLocalRandom.current().nextDouble(lo, hi);
        next = now + (long) (1000.0 / cps);
    }
}
