package fallerror.module.items;

import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    final NumberSetting delay = add(new NumberSetting("Delay", 2, 0, 10, " ticks"));
    private int timer;

    public AutoTotem() { super("Auto Totem", "Keeps a totem of undying in your offhand", Category.ITEMS); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) { timer = 0; return; }
        if (++timer < delay.get()) return;
        timer = 0;
        for (int i = 9; i <= 44; i++) {
            if (mc.player.playerScreenHandler.getSlot(i).getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, i, 40, SlotActionType.SWAP, mc.player);
                return;
            }
        }
    }
}
