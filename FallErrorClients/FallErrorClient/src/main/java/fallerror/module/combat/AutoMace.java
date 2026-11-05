package fallerror.module.combat;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.*;
import fallerror.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

/** While you fall, swaps to a hotbar mace and smashes the nearest target. */
public class AutoMace extends Module {
    final NumberSetting engage   = add(new NumberSetting("Engage Range", 5, 2, 10, " blocks"));
    final NumberSetting attackR  = add(new NumberSetting("Attack Range", 3, 2, 6, " blocks"));
    final NumberSetting minFall  = add(new NumberSetting("Min Fall Speed", 0.6, 0.2, 2.0, ""));
    final BoolSetting   rotate   = add(new BoolSetting("Rotate", true));
    final BoolSetting   switchBack = add(new BoolSetting("Switch Back", true));
    final BoolSetting   players  = add(new BoolSetting("Players", true));
    final BoolSetting   mobs     = add(new BoolSetting("Mobs", false));
    final BoolSetting   skipTeam = add(new BoolSetting("Skip Teammates", true));
    private int prevSlot = -1;

    public AutoMace() {
        super("Auto Mace", "Switches to your mace and smashes while falling", Category.COMBAT);
    }

    @Override public void onDisable() { restore(); }

    private void restore() {
        if (prevSlot != -1 && mc.player != null) Util.select(prevSlot);
        prevSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        int mace = Util.hotbarSlot(Items.MACE);
        Vec3d v = mc.player.getVelocity();
        boolean falling = !mc.player.isOnGround() && v.y < -minFall.get();

        LivingEntity target = null;
        double best = engage.get();
        if (falling && mace != -1) {
            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof LivingEntity le) || !le.isAlive()) continue;
                if (!Util.valid(le, players.get(), mobs.get(), false, skipTeam.get())) continue;
                double d = mc.player.distanceTo(le);
                if (d < best) { best = d; target = le; }
            }
        }

        if (target != null) {
            if (prevSlot == -1) prevSlot = Util.selected();
            Util.select(mace);
            if (rotate.get()) Util.face(Util.center(target));
            if (mc.player.distanceTo(target) <= attackR.get()
                    && mc.player.getAttackCooldownProgress(0f) >= 0.9f) Util.attack(target);
        } else if (switchBack.get() && (mc.player.isOnGround() || !falling)) {
            restore();
        }
    }
}
