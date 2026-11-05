package fallerror.module.client;

import fallerror.FallErrorClient;
import fallerror.gui.Theme;
import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.BoolSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;

public class Hud extends Module {
    final BoolSetting watermark = add(new BoolSetting("Watermark", true));
    final BoolSetting moduleList = add(new BoolSetting("Module List", true));

    public Hud() {
        super("HUD", "On-screen watermark and enabled module list", Category.SETTINGS);
        setEnabled(true);
    }

    public void render(DrawContext g) {
        if (!isEnabled() || mc.player == null || mc.options.hudHidden) return;
        TextRenderer tr = mc.textRenderer;
        int accent = Theme.accent();
        if (watermark.get()) g.drawText(tr, "Fall Error Client", 4, 4, accent, true);
        if (moduleList.get()) {
            List<Module> on = FallErrorClient.modules.modules.stream()
                    .filter(m -> m.isEnabled() && m.category != Category.SETTINGS)
                    .sorted(Comparator.comparingInt((Module m) -> tr.getWidth(m.name)).reversed())
                    .toList();
            int y = 4, w = g.getScaledWindowWidth();
            for (Module m : on) {
                g.drawText(tr, m.name, w - tr.getWidth(m.name) - 4, y, accent, true);
                y += 10;
            }
        }
    }
}
