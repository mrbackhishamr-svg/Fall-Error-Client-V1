package fallerror.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Projects world positions onto the screen and draws 2D boxes/labels (first person only). */
public final class Overlay {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static double ex, ey, ez, fx, fy, fz, rx, rz, ux, uy, uz, tanY, aspect;
    private static int sw, sh;
    private Overlay() {}

    public static boolean begin(DrawContext g) {
        if (mc.player == null || !mc.options.getPerspective().isFirstPerson()) return false;
        Vec3d eye = mc.player.getEyePos();
        ex = eye.x; ey = eye.y; ez = eye.z;
        double yaw = Math.toRadians(mc.player.getYaw()), pitch = Math.toRadians(mc.player.getPitch());
        double cp = Math.cos(pitch);
        fx = -Math.sin(yaw) * cp; fy = -Math.sin(pitch); fz = Math.cos(yaw) * cp;
        rx = -Math.cos(yaw);      rz = -Math.sin(yaw);
        ux = -rz * fy;            uy = rz * fx - rx * fz;   uz = rx * fy;
        sw = g.getScaledWindowWidth(); sh = g.getScaledWindowHeight();
        tanY = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
        aspect = (double) sw / sh;
        return true;
    }

    private static double[] proj(double x, double y, double z) {
        double dx = x - ex, dy = y - ey, dz = z - ez;
        double depth = dx * fx + dy * fy + dz * fz;
        double d = Math.max(depth, 0.05);
        double xr = dx * rx + dz * rz;
        double yu = dx * ux + dy * uy + dz * uz;
        return new double[]{(1 + xr / (d * tanY * aspect)) * 0.5 * sw, (1 - yu / (d * tanY)) * 0.5 * sh, depth};
    }

    /** Draws an outlined (and optionally filled) screen rectangle around a box. Returns {x1,y1,x2,y2} or null if off-screen. */
    public static int[] box(DrawContext g, Box b, int color, int fill) {
        double minX = 1e9, minY = 1e9, maxX = -1e9, maxY = -1e9;
        int ok = 0;
        for (int i = 0; i < 8; i++) {
            double[] p = proj((i & 1) == 0 ? b.minX : b.maxX, (i & 2) == 0 ? b.minY : b.maxY, (i & 4) == 0 ? b.minZ : b.maxZ);
            if (p[2] <= 0.05) continue;
            ok++;
            minX = Math.min(minX, p[0]); maxX = Math.max(maxX, p[0]);
            minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]);
        }
        if (ok == 0) return null;
        int x1 = (int) Math.max(-4000, Math.min(4000, minX)), x2 = (int) Math.max(-4000, Math.min(4000, maxX));
        int y1 = (int) Math.max(-4000, Math.min(4000, minY)), y2 = (int) Math.max(-4000, Math.min(4000, maxY));
        if (x2 < x1 + 2) x2 = x1 + 2;
        if (y2 < y1 + 2) y2 = y1 + 2;
        if (x2 < 0 || y2 < 0 || x1 > sw || y1 > sh) return null;
        if (fill != 0) g.fill(x1, y1, x2, y2, fill);
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
        return new int[]{x1, y1, x2, y2};
    }

    public static void textCentered(DrawContext g, String s, int cx, int y, int color) {
        g.drawText(mc.textRenderer, s, cx - mc.textRenderer.getWidth(s) / 2, y, color, true);
    }

    public static int faded(int color) { return (color & 0x00FFFFFF) | 0x30000000; }
}
