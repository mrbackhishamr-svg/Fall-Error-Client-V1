package fallerror.module.combat;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.*;
import fallerror.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class KillAura extends Module {
    final NumberSetting range    = add(new NumberSetting("Range", 3.5, 1, 6, " blocks"));
    final NumberSetting cooldown = add(new NumberSetting("Min Cooldown", 92, 0, 100, "%"));
    final ModeSetting   priority = add(new ModeSetting("Priority", "Closest", "Closest", "Lowest Health"));
    final BoolSetting   rotate   = add(new BoolSetting("Rotate", true));
    final BoolSetting   players  = add(new BoolSetting("Players", true));
    final BoolSetting   mobs     = add(new BoolSetting("Mobs", false));
    final BoolSetting   animals  = add(new BoolSetting("Animals", false));
    final BoolSetting   skipTeam = add(new BoolSetting("Skip Teammates", true));

    public KillAura() {
        super("Kill Aura", "Automatically attacks targets around you", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || !le.isAlive()) continue;
            if (!Util.valid(le, players.get(), mobs.get(), animals.get(), skipTeam.get())) continue;
            double d = mc.player.distanceTo(le);
            if (d > range.get()) continue;
            double score = priority.get().equals("Lowest Health") ? le.getHealth() : d;
            if (score < bestScore) { bestScore = score; best = le; }
        }
        if (best == null) return;
        if (rotate.get()) Util.face(Util.center(best));
        if (mc.player.getAttackCooldownProgress(0f) >= cooldown.get() / 100.0) Util.attack(best);
    }
}
