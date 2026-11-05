package fallerror.module.combat;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.*;
import fallerror.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

/** Switches to a hotbar spear and attacks nearby targets (jab) or holds the charge attack. */
public class AutoSpear extends Module {
    final ModeSetting   mode     = add(new ModeSetting("Mode", "Jab", "Jab", "Charge"));
    final NumberSetting range    = add(new NumberSetting("Range", 4, 2, 6, " blocks"));
    final NumberSetting cooldown = add(new NumberSetting("Min Cooldown", 92, 0, 100, "%"));
    final BoolSetting   rotate   = add(new BoolSetting("Rotate", true));
    final BoolSetting   switchBack = add(new BoolSetting("Switch Back", true));
    final BoolSetting   players  = add(new BoolSetting("Players", true));
    final BoolSetting   mobs     = add(new BoolSetting("Mobs", false));
    final BoolSetting   skipTeam = add(new BoolSetting("Skip Teammates", true));
    private int prevSlot = -1;
    private boolean charging;

    public AutoSpear() {
        super("Auto Spear", "Auto-switches to your spear and attacks (jab or charge)", Category.COMBAT);
    }

    private static boolean isSpear(ItemStack s) {
        return !s.isEmpty() && Registries.ITEM.getId(s.getItem()).getPath().endsWith("_spear");
    }

    private int spearSlot() {
        if (isSpear(mc.player.getInventory().getStack(Util.selected()))) return Util.selected();
        for (int i = 0; i < 9; i++) if (isSpear(mc.player.getInventory().getStack(i))) return i;
        return -1;
    }

    private void releaseCharge() {
        if (charging) { mc.options.useKey.setPressed(false); charging = false; }
    }

    private void restore() {
        releaseCharge();
        if (prevSlot != -1 && mc.player != null) Util.select(prevSlot);
        prevSlot = -1;
    }

    @Override public void onDisable() { restore(); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        int spear = spearSlot();
        LivingEntity target = null;
        double best = range.get();
        if (spear != -1) {
            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof LivingEntity le) || !le.isAlive()) continue;
                if (!Util.valid(le, players.get(), mobs.get(), false, skipTeam.get())) continue;
                double d = mc.player.distanceTo(le);
                if (d < best) { best = d; target = le; }
            }
        }

        if (target == null) {
            if (switchBack.get()) restore(); else releaseCharge();
            return;
        }

        if (spear != Util.selected()) {
            if (prevSlot == -1) prevSlot = Util.selected();
            Util.select(spear);
        }
        if (rotate.get()) Util.face(Util.center(target));

        if (mode.get().equals("Charge")) {
            mc.options.useKey.setPressed(true);
            charging = true;
        } else {
            releaseCharge();
            if (mc.player.getAttackCooldownProgress(0f) >= cooldown.get() / 100.0) Util.attack(target);
        }
    }
}
