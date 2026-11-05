package fallerror.module.visuals;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.render.Overlay;
import fallerror.setting.*;
import net.minecraft.block.entity.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class ESPChest extends Module {
    final NumberSetting range = add(new NumberSetting("Range", 48, 8, 128, " blocks"));
    final BoolSetting chests  = add(new BoolSetting("Chests", true));
    final BoolSetting ender   = add(new BoolSetting("Ender Chests", true));
    final BoolSetting barrels = add(new BoolSetting("Barrels", true));
    final BoolSetting shulkers = add(new BoolSetting("Shulker Boxes", true));
    final BoolSetting fill    = add(new BoolSetting("Fill", true));

    private record Entry(Box box, int color, BlockPos pos) {}
    private volatile List<Entry> cache = new ArrayList<>();
    private int timer;

    public ESPChest() {
        super("ESP Chest", "Boxes chests, barrels and shulkers through walls", Category.VISUALS);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;
        if (++timer < 20) return;
        timer = 0;
        List<Entry> out = new ArrayList<>();
        int r = (int) Math.ceil(range.get() / 16.0);
        int pcx = mc.player.getBlockPos().getX() >> 4, pcz = mc.player.getBlockPos().getZ() >> 4;
        double max2 = range.get() * range.get();
        for (int cx = pcx - r; cx <= pcx + r; cx++) {
            for (int cz = pcz - r; cz <= pcz + r; cz++) {
                WorldChunk c = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (c == null) continue;
                for (BlockEntity be : c.getBlockEntities().values()) {
                    int col;
                    if (be instanceof TrappedChestBlockEntity) { if (!chests.get()) continue; col = 0xFFFF5555; }
                    else if (be instanceof ChestBlockEntity) { if (!chests.get()) continue; col = 0xFFFFAA00; }
                    else if (be instanceof EnderChestBlockEntity) { if (!ender.get()) continue; col = 0xFFAA55FF; }
                    else if (be instanceof BarrelBlockEntity) { if (!barrels.get()) continue; col = 0xFFC8884A; }
                    else if (be instanceof ShulkerBoxBlockEntity) { if (!shulkers.get()) continue; col = 0xFFFF77DD; }
                    else continue;
                    BlockPos p = be.getPos();
                    if (mc.player.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) > max2) continue;
                    out.add(new Entry(new Box(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1), col, p));
                }
            }
        }
        cache = out;
    }

    @Override public void onDisable() { cache = new ArrayList<>(); }

    @Override
    public void onRender2D(DrawContext g) {
        if (!Overlay.begin(g)) return;
        for (Entry e : cache) Overlay.box(g, e.box(), e.color(), fill.get() ? Overlay.faded(e.color()) : 0);
    }
}
