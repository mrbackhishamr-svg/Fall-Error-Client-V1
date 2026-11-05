package fallerror.gui;

import fallerror.setting.ModeSetting;
import fallerror.setting.NumberSetting;

public final class Theme {
    public static ModeSetting accent;
    public static NumberSetting opacity;
    private Theme() {}

    public static int accent() {
        String a = accent == null ? "Lime" : accent.get();
        return switch (a) {
            case "Cyan" -> 0xFF2BD6E0;
            case "Purple" -> 0xFFB06CFF;
            case "Red" -> 0xFFFF4D4D;
            case "Orange" -> 0xFFFFA52B;
            default -> 0xFF9BE02B;
        };
    }
    public static int background() {
        int a = (int) ((opacity == null ? 94 : opacity.get()) / 100.0 * 255);
        return (a << 24) | 0x141816;
    }
}
