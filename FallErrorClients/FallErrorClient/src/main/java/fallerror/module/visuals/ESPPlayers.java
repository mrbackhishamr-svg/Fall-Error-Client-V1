package fallerror.module.visuals;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.render.Overlay;
import fallerror.setting.*;
import fallerror.util.Friends;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ESPPlayers extends Module {
    final NumberSetting range = add(new NumberSetting("Range", 128, 16, 256, " blocks"));
    final BoolSetting names    = add(new BoolSetting("Names", true));
    final BoolSetting health   = add(new BoolSetting("Health", true));
    final BoolSetting distance = add(new BoolSetting("Distance", true));
    final BoolSetting fill     = add(new BoolSetting("Fill", false));
    final BoolSetting glow     = add(new BoolSetting("Glow", false));
    final BoolSetting mobs     = add(new BoolSetting("Hostile Mobs", false));
    private boolean glowed;

    public ESPPlayers() {
        super("ESP Players", "Boxes and names on players through walls (friends are green)", Category.VISUALS);
    }

    @Override
    public void onTick() {
        if (mc.world == null) return;
        if (glow.get()) {
            for (PlayerEntity p : mc.world.getPlayers()) if (p != mc.player && !p.isGlowing()) p.setGlowing(true);
            glowed = true;
        } else if (glowed) { clearGlow(); glowed = false; }
    }

    @Override public void onDisable() { clearGlow(); glowed = false; }

    private void clearGlow() {
        if (mc.world == null) return;
        for (PlayerEntity p : mc.world.getPlayers()) if (p != mc.player) p.setGlowing(false);
    }

    @Override
    public void onRender2D(DrawContext g) {
        if (!Overlay.begin(g)) return;
        for (PlayerEntity p : mc.world.getPlayers()) draw(g, p);
        if (mobs.get()) for (Entity e : mc.world.getEntities()) if (e instanceof HostileEntity h) draw(g, h);
    }

    private void draw(DrawContext g, LivingEntity e) {
        if (e == mc.player || !e.isAlive()) return;
        double d = mc.player.distanceTo(e);
        if (d > range.get()) return;
        int col = Friends.isFriend(e) ? 0xFF55FF55 : mc.player.isTeammate(e) ? 0xFF55FFFF
                : e instanceof PlayerEntity ? 0xFFFF5555 : 0xFFFFAA00;
        int[] r = Overlay.box(g, e.getBoundingBox(), col, fill.get() ? Overlay.faded(col) : 0);
        if (r == null) return;
        StringBuilder s = new StringBuilder();
        if (names.get()) s.append(e.getName().getString());
        if (health.get()) s.append(" ").append((int) Math.ceil(e.getHealth())).append("hp");
        if (distance.get()) s.append(" ").append((int) d).append("m");
        if (s.length() > 0) Overlay.textCentered(g, s.toString().trim(), (r[0] + r[2]) / 2, r[1] - 10, col);
    }
}
