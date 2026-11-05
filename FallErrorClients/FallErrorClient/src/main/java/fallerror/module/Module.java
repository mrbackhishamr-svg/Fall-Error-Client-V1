package fallerror.module;

import fallerror.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    public final String name, description;
    public final Category category;
    public final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }
    protected <S extends Setting<?>> S add(S s) { settings.add(s); return s; }

    public boolean isEnabled() { return enabled; }
    public void toggle() { setEnabled(!enabled); }
    public void setEnabled(boolean e) {
        if (enabled == e) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
    }
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    /** Called every frame while enabled; draw 2D overlays here. */
    public void onRender2D(DrawContext g) {}
}
