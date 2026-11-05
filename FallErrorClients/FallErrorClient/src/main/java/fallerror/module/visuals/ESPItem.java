package fallerror.module.visuals;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.render.Overlay;
import fallerror.setting.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.Registries;

public class ESPItem extends Module {
    final NumberSetting range = add(new NumberSetting("Range", 48, 8, 128, " blocks"));
    final BoolSetting valuableOnly = add(new BoolSetting("Valuable Only", false));
    final BoolSetting labels = add(new BoolSetting("Labels", true));
    final BoolSetting fill   = add(new BoolSetting("Fill", true));

    public ESPItem() {
        super("ESP Item", "Boxes dropped items through walls", Category.VISUALS);
    }

    private static boolean valuable(String id) {
        return id.contains("diamond") || id.contains("netherite") || id.contains("emerald") || id.contains("gold")
                || id.contains("totem") || id.contains("elytra") || id.contains("shulker") || id.contains("enchanted")
                || id.contains("mace") || id.contains("trident") || id.contains("ancient_debris");
    }

    @Override
    public void onRender2D(DrawContext g) {
        if (!Overlay.begin(g)) return;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof ItemEntity ie) || !ie.isAlive()) continue;
            if (mc.player.distanceTo(ie) > range.get()) continue;
            String id = Registries.ITEM.getId(ie.getStack().getItem()).getPath();
            boolean val = valuable(id);
            if (valuableOnly.get() && !val) continue;
            int col = val ? 0xFFFFD700 : 0xFFFFFFFF;
            int[] r = Overlay.box(g, ie.getBoundingBox().expand(0.1), col, fill.get() ? Overlay.faded(col) : 0);
            if (r != null && labels.get()) {
                String s = ie.getStack().getName().getString() + (ie.getStack().getCount() > 1 ? " x" + ie.getStack().getCount() : "");
                Overlay.textCentered(g, s, (r[0] + r[2]) / 2, r[1] - 10, col);
            }
        }
    }
}
