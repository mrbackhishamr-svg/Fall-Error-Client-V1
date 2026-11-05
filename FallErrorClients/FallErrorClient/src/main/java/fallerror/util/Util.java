package fallerror.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class Util {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private Util() {}

    public static boolean valid(Entity e, boolean players, boolean mobs, boolean animals, boolean skipTeam) {
        if (e == mc.player) return false;
        if (Friends.isFriend(e)) return false;
        if (skipTeam && mc.player.isTeammate(e)) return false;
        if (e instanceof PlayerEntity) return players;
        if (e instanceof HostileEntity) return mobs;
        if (e instanceof AnimalEntity) return animals;
        return false;
    }

    public static Vec3d center(Entity e) {
        return new Vec3d(e.getX(), e.getY() + e.getHeight() * 0.5, e.getZ());
    }

    public static double yawTo(Vec3d t) {
        Vec3d eye = mc.player.getEyePos();
        float yaw = (float) Math.toDegrees(Math.atan2(t.z - eye.z, t.x - eye.x)) - 90f;
        return MathHelper.wrapDegrees(yaw - mc.player.getYaw());
    }

    /** Instantly look at a point. */
    public static void face(Vec3d t) {
        Vec3d eye = mc.player.getEyePos();
        double dx = t.x - eye.x, dy = t.y - eye.y, dz = t.z - eye.z;
        mc.player.setYaw((float) Math.toDegrees(Math.atan2(dz, dx)) - 90f);
        mc.player.setPitch(MathHelper.clamp((float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz))), -90f, 90f));
    }

    public static void attack(Entity e) {
        mc.interactionManager.attackEntity(mc.player, e);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    public static int hotbarSlot(Item item) {
        for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        return -1;
    }

    public static void select(int slot) { mc.player.getInventory().setSelectedSlot(slot); }
    public static int selected() { return mc.player.getInventory().getSelectedSlot(); }

    /** Horizontal movement direction from WASD relative to where the player looks (length 0..1). */
    public static double[] moveDir() {
        double f = (mc.options.forwardKey.isPressed() ? 1 : 0) - (mc.options.backKey.isPressed() ? 1 : 0);
        double s = (mc.options.rightKey.isPressed() ? 1 : 0) - (mc.options.leftKey.isPressed() ? 1 : 0);
        double yaw = Math.toRadians(mc.player.getYaw());
        double x = f * -Math.sin(yaw) + s * -Math.cos(yaw);
        double z = f * Math.cos(yaw) + s * -Math.sin(yaw);
        double len = Math.sqrt(x * x + z * z);
        if (len > 1) { x /= len; z /= len; }
        return new double[]{x, z};
    }
}
