package fallerror.module.combat;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.*;
import fallerror.util.Friends;
import fallerror.util.Util;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssist extends Module {
    final NumberSetting strength  = add(new NumberSetting("Strength", 55, 0, 100, "%"));
    final NumberSetting smoothing = add(new NumberSetting("Smoothing", 40, 0, 100, "%"));
    final NumberSetting distance  = add(new NumberSetting("Distance", 4.5, 1, 8, " blocks"));
    final NumberSetting fov       = add(new NumberSetting("FOV Cone", 45, 5, 180, "°"));
    final ModeSetting   aimAt     = add(new ModeSetting("Aim At", "Closest", "Closest", "Head", "Body"));
    final BoolSetting   onlyAttacking = add(new BoolSetting("Only While Attacking", true));
    final BoolSetting   horizontalOnly = add(new BoolSetting("Horizontal Only", false));
    final BoolSetting   skipTeam = add(new BoolSetting("Skip Teammates", true));

    public AimAssist() {
        super("Aim Assist", "Eases your aim onto the player nearest your crosshair", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (onlyAttacking.get() && !mc.options.attackKey.isPressed()) return;

        PlayerEntity best = null;
        double bestAngle = fov.get() / 2.0;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            if (skipTeam.get() && mc.player.isTeammate(p)) continue;
            if (Friends.isFriend(p)) continue;
            if (mc.player.distanceTo(p) > distance.get()) continue;
            double a = Math.abs(Util.yawTo(aimPoint(p)));
            if (a <= bestAngle) { bestAngle = a; best = p; }
        }
        if (best == null) return;

        Vec3d t = aimPoint(best);
        Vec3d eye = mc.player.getEyePos();
        double dx = t.x - eye.x, dy = t.y - eye.y, dz = t.z - eye.z;
        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));

        float step = (float) ((strength.get() / 100.0) * (1.0 - smoothing.get() / 105.0)) * 0.35f;
        float dYaw = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        mc.player.setYaw(mc.player.getYaw() + dYaw * step);
        if (!horizontalOnly.get()) {
            float dPitch = targetPitch - mc.player.getPitch();
            mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + dPitch * step, -90f, 90f));
        }
    }

    private Vec3d aimPoint(PlayerEntity p) {
        Vec3d head = p.getEyePos(), body = Util.center(p);
        return switch (aimAt.get()) {
            case "Head" -> head;
            case "Body" -> body;
            default -> Math.abs(Util.yawTo(head)) <= Math.abs(Util.yawTo(body)) ? head : body;
        };
    }
}
