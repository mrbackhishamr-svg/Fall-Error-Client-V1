package fallerror.module.visuals;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.render.Overlay;
import fallerror.setting.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Highlights ores through walls with a screen overlay (does not make stone transparent). */
public class Xray extends Module {
    final NumberSetting radius = add(new NumberSetting("Radius", 16, 8, 32, " blocks"));
    final BoolSetting diamond = add(new BoolSetting("Diamond", true));
    final BoolSetting debris  = add(new BoolSetting("Ancient Debris", true));
    final BoolSetting emerald = add(new BoolSetting("Emerald", true));
    final BoolSetting gold    = add(new BoolSetting("Gold", true));
    final BoolSetting iron    = add(new BoolSetting("Iron", false));
    final BoolSetting other   = add(new BoolSetting("Other Ores", false));
    final BoolSetting labels  = add(new BoolSetting("Labels", false));
    final BoolSetting fill    = add(new BoolSetting("Fill", true));

    private record Entry(Box box, int color, String name) {}
    private volatile List<Entry> cache = new ArrayList<>();
    private final Map<Block, Integer> kinds = new HashMap<>(); // 0 none, 1 diamond, 2 debris, 3 emerald, 4 gold, 5 iron, 6 other
    private int timer;

    public Xray() {
        super("Xray", "Highlights ores through walls", Category.VISUALS);
    }

    private int kind(Block b) {
        return kinds.computeIfAbsent(b, k -> {
            String id = Registries.BLOCK.getId(k).getPath();
            if (id.equals("ancient_debris")) return 2;
            if (!id.endsWith("_ore")) return 0;
            if (id.contains("diamond")) return 1;
            if (id.contains("emerald")) return 3;
            if (id.contains("gold")) return 4;
            if (id.contains("iron")) return 5;
            return 6;
        });
    }

    private int colorFor(int kind) {
        return switch (kind) {
            case 1 -> diamond.get() ? 0xFF55FFFF : 0;
            case 2 -> debris.get()  ? 0xFFC47A3A : 0;
            case 3 -> emerald.get() ? 0xFF2ECC40 : 0;
            case 4 -> gold.get()    ? 0xFFFFD700 : 0;
            case 5 -> iron.get()    ? 0xFFD8AF93 : 0;
            case 6 -> other.get()   ? 0xFFBBBBBB : 0;
            default -> 0;
        };
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;
        if (++timer < 20) return;
        timer = 0;
        List<Entry> out = new ArrayList<>();
        int r = (int) Math.round(radius.get());
        BlockPos c = mc.player.getBlockPos();
        BlockPos.Mutable m = new BlockPos.Mutable();
        outer:
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    m.set(c.getX() + x, c.getY() + y, c.getZ() + z);
                    BlockState s = mc.world.getBlockState(m);
                    if (s.isAir()) continue;
                    int k = kind(s.getBlock());
                    if (k == 0) continue;
                    int col = colorFor(k);
                    if (col == 0) continue;
                    out.add(new Entry(new Box(m.getX(), m.getY(), m.getZ(), m.getX() + 1, m.getY() + 1, m.getZ() + 1),
                            col, s.getBlock().getName().getString()));
                    if (out.size() >= 500) break outer;
                }
            }
        }
        cache = out;
    }

    @Override public void onDisable() { cache = new ArrayList<>(); }

    @Override
    public void onRender2D(DrawContext g) {
        if (!Overlay.begin(g)) return;
        for (Entry e : cache) {
            int[] r = Overlay.box(g, e.box(), e.color(), fill.get() ? Overlay.faded(e.color()) : 0);
            if (r != null && labels.get()) Overlay.textCentered(g, e.name(), (r[0] + r[2]) / 2, r[1] - 10, e.color());
        }
    }
}
