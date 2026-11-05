package fallerror.gui;

import fallerror.FallErrorClient;
import fallerror.module.Category;
import fallerror.module.Module;
import fallerror.setting.*;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

/** Dark two-pane menu: category tabs on top, module list left, settings right. */
public class ClickGui extends Screen {
    static final int PANEL = 0xFF1B201D, TEXT = 0xFFE6E6E6, DIM = 0xFF8A918C, TRACK = 0xFF3A403C;
    static final int W = 430, H = 290, SIDE = 105, ROW = 22, SROW = 18, CTRL = 125, SW = 100;

    private Category tab = Category.COMBAT;
    private Module selected;
    private NumberSetting dragging;
    private int x0, y0;

    public ClickGui() { super(Text.literal(FallErrorClient.NAME)); }

    @Override protected void init() { x0 = (width - W) / 2; y0 = (height - H) / 2; }
    @Override public boolean shouldPause() { return false; }

    private List<Module> mods() { return FallErrorClient.modules.byCategory(tab); }
    private static String label(Category c) { return c.name().charAt(0) + c.name().substring(1).toLowerCase(); }

    @Override
    public void render(DrawContext g, int mx, int my, float delta) {
        int accent = Theme.accent();
        g.fill(x0, y0, x0 + W, y0 + H, Theme.background());
        g.drawText(textRenderer, "fall error", x0 + 10, y0 + 8, TEXT, true);
        int tx = x0 + 90;
        for (Category c : Category.values()) {
            g.drawText(textRenderer, label(c), tx, y0 + 8, c == tab ? accent : DIM, false);
            tx += textRenderer.getWidth(label(c)) + 14;
        }
        int y = y0 + 28;
        for (Module m : mods()) {
            if (m == selected) g.fill(x0 + 6, y - 2, x0 + SIDE, y + 12, PANEL);
            g.drawText(textRenderer, "●", x0 + 10, y, m.isEnabled() ? accent : TRACK, false);
            g.drawText(textRenderer, m.name, x0 + 24, y, TEXT, false);
            y += ROW;
        }
        g.fill(x0 + SIDE + 8, y0 + 24, x0 + W - 8, y0 + H - 8, PANEL);
        if (selected == null) return;
        int px = x0 + SIDE + 20;
        g.drawText(textRenderer, selected.name, px, y0 + 32, TEXT, true);
        g.drawText(textRenderer, selected.description, px, y0 + 44, DIM, false);
        drawToggle(g, x0 + W - 40, y0 + 32, selected.isEnabled(), accent);

        int sy = y0 + 64;
        for (Setting<?> s : selected.settings) {
            g.drawText(textRenderer, s.name, px, sy, DIM, false);
            int cx = px + CTRL;
            if (s instanceof NumberSetting n) {
                int knob = (int) (SW * n.percent());
                g.fill(cx, sy + 3, cx + SW, sy + 5, TRACK);
                g.fill(cx, sy + 3, cx + knob, sy + 5, accent);
                g.fill(cx + knob - 2, sy, cx + knob + 2, sy + 8, accent);
                String v = (n.get() % 1 == 0 ? String.valueOf(n.get().intValue()) : String.format("%.2f", n.get())) + n.suffix;
                g.drawText(textRenderer, v, x0 + W - 18 - textRenderer.getWidth(v), sy, TEXT, false);
            } else if (s instanceof BoolSetting b) {
                g.fill(cx, sy, cx + 10, sy + 10, b.get() ? accent : TRACK);
            } else if (s instanceof ModeSetting m) {
                g.drawText(textRenderer, "< " + m.get() + " >", cx, sy, accent, false);
            }
            sy += SROW;
        }
    }

    private void drawToggle(DrawContext g, int x, int y, boolean on, int accent) {
        g.fill(x, y, x + 24, y + 12, on ? accent : TRACK);
        g.fill(on ? x + 14 : x + 2, y + 2, on ? x + 22 : x + 10, y + 10, 0xFF101310);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int button = click.button();
        int tx = x0 + 90;
        for (Category c : Category.values()) {
            int w = textRenderer.getWidth(label(c));
            if (mx >= tx && mx <= tx + w && my >= y0 + 4 && my <= y0 + 20) { tab = c; selected = null; return true; }
            tx += w + 14;
        }
        int y = y0 + 28;
        for (Module m : mods()) {
            if (mx >= x0 + 6 && mx <= x0 + SIDE && my >= y - 2 && my <= y + 12) {
                if (button == 1 || mx < x0 + 20) m.toggle(); else selected = m;
                return true;
            }
            y += ROW;
        }
        if (selected == null) return super.mouseClicked(click, doubled);
        if (mx >= x0 + W - 40 && mx <= x0 + W - 16 && my >= y0 + 32 && my <= y0 + 44) { selected.toggle(); return true; }
        int px = x0 + SIDE + 20, sy = y0 + 64;
        for (Setting<?> s : selected.settings) {
            if (my >= sy - 2 && my <= sy + 11) {
                int cx = px + CTRL;
                if (s instanceof NumberSetting n && mx >= cx - 4 && mx <= cx + SW + 4) { dragging = n; applyDrag(n, mx); return true; }
                if (s instanceof BoolSetting b && mx >= cx && mx <= cx + 10) { b.toggle(); return true; }
                if (s instanceof ModeSetting m && mx >= cx) {
                    int i = Arrays.asList(m.modes).indexOf(m.get());
                    m.set(m.modes[(i + 1) % m.modes.length]); return true;
                }
            }
            sy += SROW;
        }
        return super.mouseClicked(click, doubled);
    }

    private void applyDrag(NumberSetting n, double mx) {
        double p = Math.max(0, Math.min(1, (mx - (x0 + SIDE + 20 + CTRL)) / SW));
        double v = n.min + (n.max - n.min) * p;
        n.setClamped(Math.round(v * 100.0) / 100.0);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (dragging != null) { applyDrag(dragging, click.x()); return true; }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) { dragging = null; return super.mouseReleased(click); }
}
