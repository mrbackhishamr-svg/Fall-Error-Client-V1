package fallerror.module.client;

import fallerror.gui.Theme;
import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.*;

public class Interface extends Module {
    public Interface() {
        super("Interface", "Menu look and feel", Category.SETTINGS);
        Theme.accent = add(new ModeSetting("Accent", "Lime", "Lime", "Cyan", "Purple", "Red", "Orange"));
        Theme.opacity = add(new NumberSetting("Background", 94, 40, 100, "%"));
        setEnabled(true);
    }
}
